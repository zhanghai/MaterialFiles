/*
 * Copyright (C) 2012-2015 Jorrit "Chainfire" Jongma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

// ZH: Make stderr a first-class stream.

package me.zhanghai.android.files.shell;

import android.os.Handler;
import android.os.Looper;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import eu.chainfire.libsuperuser.Debug;
import eu.chainfire.libsuperuser.ShellNotClosedException;
import eu.chainfire.libsuperuser.ShellOnMainThreadException;
import eu.chainfire.libsuperuser.StreamGobbler;

/**
 * <p>
 * An interactive shell - initially created with {@link Builder} -
 * that executes blocks of commands you supply in the background, optionally
 * calling callbacks as each block completes.
 * </p>
 * <p>
 * stderr output can be supplied as well, but due to compatibility with
 * older Android versions, wantStderr is not implemented using
 * redirectErrorStream, but rather appended to the output. stdout and stderr
 * are thus not guaranteed to be in the correct order in the output.
 * </p>
 * <p>
 * Note as well that the close() and waitForIdle() methods will
 * intentionally crash when run in debug mode from the main thread of the
 * application. Any blocking call should be run from a background thread.
 * </p>
 * <p>
 * When in debug mode, the code will also excessively log the commands
 * passed to and the output returned from the shell.
 * </p>
 * <p>
 * Though this function uses background threads to gobble stdout and stderr
 * so a deadlock does not occur if the shell produces massive output, the
 * output is still stored in a List&lt;String&gt;, and as such doing
 * something like <em>'ls -lR /'</em> will probably have you run out of
 * memory when using a {@link OnCommandResultListener}. A work-around
 * is to not supply this callback, but using (only)
 * {@link Builder#setOnStdoutLineListener(StreamGobbler.OnLineListener)}. This way,
 * an internal stdoutBuffer will not be created and wasting your memory.
 * </p>
 * <h3>Callbacks, threads and handlers</h3>
 * <p>
 * On which thread the callbacks execute is dependent on your
 * initialization. You can supply a custom Handler using
 * {@link Builder#setHandler(Handler)} if needed. If you do not supply
 * a custom Handler - unless you set
 * {@link Builder#setAutoHandler(boolean)} to false - a Handler will
 * be auto-created if the thread used for instantiation of the object has a
 * Looper.
 * </p>
 * <p>
 * If no Handler was supplied and it was also not auto-created, all
 * callbacks will be called from either the stdout or stderr gobbler
 * threads. These are important threads that should be blocked as little as
 * possible, as blocking them may in rare cases pause the native process or
 * even create a deadlock.
 * </p>
 * <p>
 * The main thread must certainly have a Looper, thus if you call
 * {@link Builder#open()} from the main thread, a handler will (by
 * default) be auto-created, and all the callbacks will be called on the
 * main thread. While this is often convenient and easy to code with, you
 * should be aware that if your callbacks are 'expensive' to execute, this
 * may negatively impact UI performance.
 * </p>
 * <p>
 * Background threads usually do <em>not</em> have a Looper, so calling
 * {@link Builder#open()} from such a background thread will (by
 * default) result in all the callbacks being executed in one of the gobbler
 * threads. You will have to make sure the code you execute in these
 * callbacks is thread-safe.
 * </p>
 */
public class Shell {

    private final Handler handler;
    private final boolean autoHandler;
    private final String shell;
    private final boolean wantStderr;
    private final List<Command> commands;
    private final Map<String, String> environment;
    private final StreamGobbler.OnLineListener onStdoutLineListener;
    private final StreamGobbler.OnLineListener onStderrLineListener;
    private int watchdogTimeout;

    private Process process = null;
    private DataOutputStream stdin = null;
    private StreamGobbler stdout = null;
    private StreamGobbler stderr = null;
    private ScheduledThreadPoolExecutor watchdog = null;

    private volatile boolean running = false;
    private volatile boolean idle = true; // read/write only synchronized
    private volatile boolean closed = true;
    private volatile int callbacks = 0;
    private volatile int watchdogCount;

    private final Object idleSync = new Object();
    private final Object callbackSync = new Object();

    private volatile int lastExitCode = 0;
    private volatile String lastMarkerStdout = null;
    private volatile String lastMarkerStderr = null;
    private volatile Command command = null;
    private volatile List<String> stdoutBuffer = null;
    private volatile List<String> stderrBuffer = null;

    /**
     * The only way to create an instance: Shell.Builder::open()
     *
     * @param builder Builder class to take values from
     */
    Shell(Builder builder, OnCommandResultListener onCommandResultListener) {
        autoHandler = builder.autoHandler;
        shell = builder.shell;
        wantStderr = builder.wantStderr;
        commands = builder.commands;
        environment = builder.environment;
        onStdoutLineListener = builder.onStdoutLineListener;
        onStderrLineListener = builder.onStderrLineListener;
        watchdogTimeout = builder.watchdogTimeout;

        // If a looper is available, we offload the callbacks from the
        // gobbling threads
        // to whichever thread created us. Would normally do this in open(),
        // but then we could not declare handler as final
        if ((Looper.myLooper() != null) && (builder.handler == null) && autoHandler) {
            handler = new Handler();
        } else {
            handler = builder.handler;
        }

        if (onCommandResultListener != null) {
            // Allow up to 60 seconds for SuperSU/Superuser dialog, then enable
            // the user-specified timeout for all subsequent operations
            watchdogTimeout = 60;
            commands.add(0, new Command(availableTestCommands, 0,
                    (commandCode, exitCode, stdout, stderr) -> {
                        if ((exitCode == OnCommandResultListener.SHELL_RUNNING) &&
                                !parseAvailableResult(stdout, isSU(shell))) {
                            // shell is up, but it's brain-damaged
                            exitCode = OnCommandResultListener.SHELL_WRONG_UID;
                        }
                        watchdogTimeout = builder.watchdogTimeout;
                        onCommandResultListener.onCommandResult(0, exitCode, stdout, stderr);
                    }, null));
        }

        if (!open() && (onCommandResultListener != null)) {
            onCommandResultListener.onCommandResult(0,
                    OnCommandResultListener.SHELL_EXEC_FAILED, null, null);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        if (!closed && Debug.getSanityChecksEnabledEffective()) {
            // waste of resources
            Debug.log(ShellNotClosedException.EXCEPTION_NOT_CLOSED);
            throw new ShellNotClosedException();
        }
        super.finalize();
    }

    /**
     * Add a command to execute
     *
     * @param command Command to execute
     */
    public void addCommand(String command) {
        addCommand(command, 0, (OnCommandResultListener) null);
    }

    /**
     * <p>
     * Add a command to execute, with a callback to be called on completion
     * </p>
     * <p>
     * The thread on which the callback executes is dependent on various
     * factors, see {@link Shell} for further details
     * </p>
     *
     * @param command                 Command to execute
     * @param code                    User-defined value passed back to the callback
     * @param onCommandResultListener Callback to be called on completion
     */
    public void addCommand(String command, int code,
                           OnCommandResultListener onCommandResultListener) {
        addCommand(new String[] { command }, code, onCommandResultListener);
    }

    /**
     * <p>
     * Add a command to execute, with a callback. This callback gobbles the
     * output line by line without buffering it and also returns the result
     * code on completion.
     * </p>
     * <p>
     * The thread on which the callback executes is dependent on various
     * factors, see {@link Shell} for further details
     * </p>
     *
     * @param command               Command to execute
     * @param code                  User-defined value passed back to the callback
     * @param onCommandLineListener Callback
     */
    public void addCommand(String command, int code, OnCommandLineListener onCommandLineListener) {
        addCommand(new String[] { command }, code, onCommandLineListener);
    }

    /**
     * Add commands to execute
     *
     * @param commands Commands to execute
     */
    public void addCommand(List<String> commands) {
        addCommand(commands, 0, (OnCommandResultListener) null);
    }

    /**
     * <p>
     * Add commands to execute, with a callback to be called on completion
     * (of all commands)
     * </p>
     * <p>
     * The thread on which the callback executes is dependent on various
     * factors, see {@link Shell} for further details
     * </p>
     *
     * @param commands                Commands to execute
     * @param code                    User-defined value passed back to the callback
     * @param onCommandResultListener Callback to be called on completion
     *                                (of all commands)
     */
    public void addCommand(List<String> commands, int code,
                           OnCommandResultListener onCommandResultListener) {
        addCommand(commands.toArray(new String[commands.size()]), code, onCommandResultListener);
    }

    /**
     * <p>
     * Add commands to execute, with a callback. This callback gobbles the
     * output line by line without buffering it and also returns the result
     * code on completion.
     * </p>
     * <p>
     * The thread on which the callback executes is dependent on various
     * factors, see {@link Shell} for further details
     * </p>
     *
     * @param commands              Commands to execute
     * @param code                  User-defined value passed back to the callback
     * @param onCommandLineListener Callback
     */
    public void addCommand(List<String> commands, int code,
                           OnCommandLineListener onCommandLineListener) {
        addCommand(commands.toArray(new String[commands.size()]), code, onCommandLineListener);
    }

    /**
     * Add commands to execute
     *
     * @param commands Commands to execute
     */
    public void addCommand(String[] commands) {
        addCommand(commands, 0, (OnCommandResultListener) null);
    }

    /**
     * <p>
     * Add commands to execute, with a callback to be called on completion
     * (of all commands)
     * </p>
     * <p>
     * The thread on which the callback executes is dependent on various
     * factors, see {@link Shell} for further details
     * </p>
     *
     * @param commands                Commands to execute
     * @param code                    User-defined value passed back to the callback
     * @param onCommandResultListener Callback to be called on completion
     *                                (of all commands)
     */
    public synchronized void addCommand(String[] commands, int code,
                                        OnCommandResultListener onCommandResultListener) {
        this.commands.add(new Command(commands, code, onCommandResultListener, null));
        runNextCommand();
    }

    /**
     * <p>
     * Add commands to execute, with a callback. This callback gobbles the
     * output line by line without buffering it and also returns the result
     * code on completion.
     * </p>
     * <p>
     * The thread on which the callback executes is dependent on various
     * factors, see {@link Shell} for further details
     * </p>
     *
     * @param commands              Commands to execute
     * @param code                  User-defined value passed back to the callback
     * @param onCommandLineListener Callback
     */
    public synchronized void addCommand(String[] commands, int code,
                                        OnCommandLineListener onCommandLineListener) {
        this.commands.add(new Command(commands, code, null, onCommandLineListener));
        runNextCommand();
    }

    /**
     * Run the next command if any and if ready, signals idle state if no
     * commands left
     */
    private void runNextCommand() {
        runNextCommand(true);
    }

    /**
     * Called from a ScheduledThreadPoolExecutor timer thread every second
     * when there is an outstanding command
     */
    private synchronized void handleWatchdog() {
        final int exitCode;

        if (watchdog == null)
            return;
        if (watchdogTimeout == 0)
            return;

        if (!isRunning()) {
            exitCode = OnCommandResultListener.SHELL_DIED;
            Debug.log(String.format("[%s%%] SHELL_DIED", shell.toUpperCase(Locale.ENGLISH)));
        } else if (watchdogCount++ < watchdogTimeout) {
            return;
        } else {
            exitCode = OnCommandResultListener.WATCHDOG_EXIT;
            Debug.log(String.format("[%s%%] WATCHDOG_EXIT", shell.toUpperCase(Locale.ENGLISH)));
        }

        postCallback(command, exitCode, stdoutBuffer, stderrBuffer);

        // prevent multiple callbacks for the same command
        command = null;
        stdoutBuffer = null;
        stderrBuffer = null;
        idle = true;

        watchdog.shutdown();
        watchdog = null;
        kill();
    }

    /**
     * Start the periodic timer when a command is submitted
     */
    private void startWatchdog() {
        if (watchdogTimeout == 0) {
            return;
        }
        watchdogCount = 0;
        watchdog = new ScheduledThreadPoolExecutor(1);
        watchdog.scheduleAtFixedRate(this::handleWatchdog, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Disable the watchdog timer upon command completion
     */
    private void stopWatchdog() {
        if (watchdog != null) {
            watchdog.shutdownNow();
            watchdog = null;
        }
    }

    /**
     * Run the next command if any and if ready
     *
     * @param notifyIdle signals idle state if no commands left ?
     */
    private void runNextCommand(boolean notifyIdle) {
        // must always be called from a synchronized method

        boolean running = isRunning();
        if (!running) {
            idle = true;
        }

        if (running && idle && (commands.size() > 0)) {
            Command command = commands.get(0);
            commands.remove(0);

            stdoutBuffer = null;
            stderrBuffer = null;
            lastExitCode = 0;
            lastMarkerStdout = null;
            lastMarkerStderr = null;

            if (command.commands.length > 0) {
                try {
                    if (command.onCommandResultListener != null) {
                        // no reason to store the output if we don't have an
                        // OnCommandResultListener
                        // user should catch the output with an
                        // OnLineListener in this case
                        stdoutBuffer = Collections.synchronizedList(new ArrayList<String>());
                        if (!wantStderr) {
                            stderrBuffer = Collections.synchronizedList(new ArrayList<String>());
                        }
                    }

                    idle = false;
                    this.command = command;
                    startWatchdog();
                    for (String write : command.commands) {
                        Debug.logCommand(String.format("[%s+] %s",
                                shell.toUpperCase(Locale.ENGLISH), write));
                        stdin.write((write + "\n").getBytes("UTF-8"));
                    }
                    stdin.write(("echo " + command.marker + " $?\n").getBytes("UTF-8"));
                    stdin.write(("echo " + command.marker + " >&2\n").getBytes("UTF-8"));
                    stdin.flush();
                } catch (IOException e) {
                    // stdin might have closed
                }
            } else {
                runNextCommand(false);
            }
        } else if (!running) {
            // our shell died for unknown reasons - abort all submissions
            while (commands.size() > 0) {
                postCallback(commands.remove(0), OnCommandResultListener.SHELL_DIED, null, null);
            }
        }

        if (idle && notifyIdle) {
            synchronized (idleSync) {
                idleSync.notifyAll();
            }
        }
    }

    /**
     * Processes a stdout/stderr line containing an end/exitCode marker
     */
    private synchronized void processMarker() {
        if (command.marker.equals(lastMarkerStdout) && (command.marker.equals(lastMarkerStderr))) {
            postCallback(command, lastExitCode, stdoutBuffer, stderrBuffer);
            stopWatchdog();
            command = null;
            stdoutBuffer = null;
            stderrBuffer = null;
            idle = true;
            runNextCommand();
        }
    }

    /**
     * Process a normal stdout/stderr line
     *
     * @param line     Line to process
     * @param listener Callback to call or null
     */
    private synchronized void processLine(String line, StreamGobbler.OnLineListener listener) {
        if (listener != null) {
            if (handler != null) {
                final String fLine = line;
                final StreamGobbler.OnLineListener fListener = listener;

                startCallback();
                handler.post(() -> {
                    try {
                        fListener.onLine(fLine);
                    } finally {
                        endCallback();
                    }
                });
            } else {
                listener.onLine(line);
            }
        }
    }

    /**
     * Add line to internal stdoutBuffer
     *
     * @param line Line to add
     */
    private synchronized void addToStdoutBuffer(String line) {
        if (stdoutBuffer != null) {
            stdoutBuffer.add(line);
        }
    }

    /**
     * Add line to internal stderrBuffer
     *
     * @param line Line to add
     */
    private synchronized void addToStderrBuffer(String line) {
        if (stderrBuffer != null) {
            stderrBuffer.add(line);
        }
    }

    /**
     * Increase callback counter
     */
    private void startCallback() {
        synchronized (callbackSync) {
            callbacks++;
        }
    }

    /**
     * Schedule a callback to run on the appropriate thread
     */
    private void postCallback(final Command fCommand, final int fExitCode,
                              final List<String> fStdout, final List<String> fStderr) {
        if (fCommand.onCommandResultListener == null && fCommand.onCommandLineListener == null) {
            return;
        }
        if (handler == null) {
            if (fCommand.onCommandResultListener != null) {
                fCommand.onCommandResultListener.onCommandResult(fCommand.code, fExitCode,
                        fStdout, fStderr);
            }
            if (fCommand.onCommandLineListener != null) {
                fCommand.onCommandLineListener.onCommandResult(fCommand.code, fExitCode);
            }
            return;
        }
        startCallback();
        handler.post(() -> {
            try {
                if (fCommand.onCommandResultListener != null) {
                    fCommand.onCommandResultListener.onCommandResult(fCommand.code,
                            fExitCode, fStdout, fStderr);
                }
                if (fCommand.onCommandLineListener != null) {
                    fCommand.onCommandLineListener
                            .onCommandResult(fCommand.code, fExitCode);
                }
            } finally {
                endCallback();
            }
        });
    }

    /**
     * Decrease callback counter, signals callback complete state when
     * dropped to 0
     */
    private void endCallback() {
        synchronized (callbackSync) {
            callbacks--;
            if (callbacks == 0) {
                callbackSync.notifyAll();
            }
        }
    }

    /**
     * Internal call that launches the shell, starts gobbling, and starts
     * executing commands. See {@link Shell}
     *
     * @return Opened successfully ?
     */
    private synchronized boolean open() {
        Debug.log(String.format("[%s%%] START", shell.toUpperCase(Locale.ENGLISH)));

        try {
            // setup our process, retrieve stdin stream, and stdout/stderr
            // gobblers
            if (environment.size() == 0) {
                process = Runtime.getRuntime().exec(shell);
            } else {
                Map<String, String> newEnvironment = new HashMap<String, String>();
                newEnvironment.putAll(System.getenv());
                newEnvironment.putAll(environment);
                int i = 0;
                String[] env = new String[newEnvironment.size()];
                for (Map.Entry<String, String> entry : newEnvironment.entrySet()) {
                    env[i] = entry.getKey() + "=" + entry.getValue();
                    i++;
                }
                process = Runtime.getRuntime().exec(shell, env);
            }

            stdin = new DataOutputStream(process.getOutputStream());
            stdout = new StreamGobbler(shell.toUpperCase(Locale.ENGLISH) + "-",
                    process.getInputStream(), line -> {
                synchronized (Shell.this) {
                    if (command == null) {
                        return;
                    }

                    String contentPart = line;
                    String markerPart = null;

                    int markerIndex = line.indexOf(command.marker);
                    if (markerIndex == 0) {
                        contentPart = null;
                        markerPart = line;
                    } else if (markerIndex > 0) {
                        contentPart = line.substring(0, markerIndex);
                        markerPart = line.substring(markerIndex);
                    }

                    if (contentPart != null) {
                        addToStdoutBuffer(contentPart);
                        processLine(contentPart, onStdoutLineListener);
                        if (command.onCommandLineListener != null) {
                            processLine(contentPart, command.onCommandLineListener::onStdoutLine);
                        }
                    }

                    if (markerPart != null) {
                        try {
                            lastExitCode = Integer.valueOf(
                                    markerPart.substring(command.marker.length() + 1), 10);
                        } catch (Exception e) {
                            // this really shouldn't happen
                            e.printStackTrace();
                        }
                        lastMarkerStdout = command.marker;
                        processMarker();
                    }
                }
            });
            stderr = new StreamGobbler(shell.toUpperCase(Locale.ENGLISH) + "*",
                    process.getErrorStream(), line -> {
                synchronized (Shell.this) {
                    if (command == null) {
                        return;
                    }

                    String contentPart = line;

                    int markerIndex = line.indexOf(command.marker);
                    if (markerIndex == 0) {
                        contentPart = null;
                    } else if (markerIndex > 0) {
                        contentPart = line.substring(0, markerIndex);
                    }

                    if (contentPart != null) {
                        if (wantStderr) {
                            addToStdoutBuffer(contentPart);
                        } else {
                            addToStderrBuffer(contentPart);
                        }
                        processLine(contentPart, onStderrLineListener);
                        if (command.onCommandLineListener != null) {
                            processLine(contentPart, command.onCommandLineListener::onStderrLine);
                        }
                    }

                    if (markerIndex >= 0) {
                        lastMarkerStderr = command.marker;
                        processMarker();
                    }
                }
            });

            // start gobbling and write our commands to the shell
            stdout.start();
            stderr.start();

            running = true;
            closed = false;

            runNextCommand();

            return true;
        } catch (IOException e) {
            // shell probably not found
            return false;
        }
    }

    /**
     * Close shell and clean up all resources. Call this when you are done
     * with the shell. If the shell is not idle (all commands completed) you
     * should not call this method from the main UI thread because it may
     * block for a long time. This method will intentionally crash your app
     * (if in debug mode) if you try to do this anyway.
     */
    public void close() {
        boolean _idle = isIdle(); // idle must be checked synchronized

        synchronized (this) {
            if (!running)
                return;
            running = false;
            closed = true;
        }

        // This method should not be called from the main thread unless the
        // shell is idle and can be cleaned up with (minimal) waiting. Only
        // throw in debug mode.
        if (!_idle && Debug.getSanityChecksEnabledEffective() && Debug.onMainThread()) {
            Debug.log(ShellOnMainThreadException.EXCEPTION_NOT_IDLE);
            throw new ShellOnMainThreadException(ShellOnMainThreadException.EXCEPTION_NOT_IDLE);
        }

        if (!_idle)
            waitForIdle();

        try {
            try {
                stdin.write(("exit\n").getBytes("UTF-8"));
                stdin.flush();
            } catch (IOException e) {
                if (e.getMessage().contains("EPIPE") || e.getMessage().contains("Stream closed")) {
                    // we're not running a shell, the shell closed stdin,
                    // the script already contained the exit command, etc.
                } else {
                    throw e;
                }
            }

            // wait for our process to finish, while we gobble away in the
            // background
            process.waitFor();

            // make sure our threads are done gobbling, our streams are
            // closed, and the process is destroyed - while the latter two
            // shouldn't be needed in theory, and may even produce warnings,
            // in "normal" Java they are required for guaranteed cleanup of
            // resources, so lets be safe and do this on Android as well
            try {
                stdin.close();
            } catch (IOException e) {
                // stdin going missing is no reason to abort
            }
            stdout.join();
            stderr.join();
            stopWatchdog();
            process.destroy();
        } catch (IOException e) {
            // various unforseen IO errors may still occur
        } catch (InterruptedException e) {
            // this should really be re-thrown
        }

        Debug.log(String.format("[%s%%] END", shell.toUpperCase(Locale.ENGLISH)));
    }

    /**
     * Try to clean up as much as possible from a shell that's gotten itself
     * wedged. Hopefully the StreamGobblers will croak on their own when the
     * other side of the pipe is closed.
     */
    public synchronized void kill() {
        running = false;
        closed = true;

        try {
            stdin.close();
        } catch (IOException e) {
            // in case it was closed
        }
        try {
            process.destroy();
        } catch (Exception e) {
            // in case it was already destroyed or can't be
        }

        idle = true;
        synchronized (idleSync) {
            idleSync.notifyAll();
        }
    }

    /**
     * Is our shell still running ?
     *
     * @return Shell running ?
     */
    public boolean isRunning() {
        if (process == null) {
            return false;
        }
        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            // if this is thrown, we're still running
        }
        return true;
    }

    /**
     * Have all commands completed executing ?
     *
     * @return Shell idle ?
     */
    public synchronized boolean isIdle() {
        if (!isRunning()) {
            idle = true;
            synchronized (idleSync) {
                idleSync.notifyAll();
            }
        }
        return idle;
    }

    /**
     * <p>
     * Wait for idle state. As this is a blocking call, you should not call
     * it from the main UI thread. If you do so and debug mode is enabled,
     * this method will intentionally crash your app.
     * </p>
     * <p>
     * If not interrupted, this method will not return until all commands
     * have finished executing. Note that this does not necessarily mean
     * that all the callbacks have fired yet.
     * </p>
     * <p>
     * If no Handler is used, all callbacks will have been executed when
     * this method returns. If a Handler is used, and this method is called
     * from a different thread than associated with the Handler's Looper,
     * all callbacks will have been executed when this method returns as
     * well. If however a Handler is used but this method is called from the
     * same thread as associated with the Handler's Looper, there is no way
     * to know.
     * </p>
     * <p>
     * In practice this means that in most simple cases all callbacks will
     * have completed when this method returns, but if you actually depend
     * on this behavior, you should make certain this is indeed the case.
     * </p>
     * <p>
     * See {@link Shell} for further details on threading and
     * handlers
     * </p>
     *
     * @return True if wait complete, false if wait interrupted
     */
    public boolean waitForIdle() {
        if (Debug.getSanityChecksEnabledEffective() && Debug.onMainThread()) {
            Debug.log(ShellOnMainThreadException.EXCEPTION_WAIT_IDLE);
            throw new ShellOnMainThreadException(ShellOnMainThreadException.EXCEPTION_WAIT_IDLE);
        }

        if (isRunning()) {
            synchronized (idleSync) {
                while (!idle) {
                    try {
                        idleSync.wait();
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
            }

            if ((handler != null) &&
                    (handler.getLooper() != null) &&
                    (handler.getLooper() != Looper.myLooper())) {
                // If the callbacks are posted to a different thread than
                // this one, we can wait until all callbacks have called
                // before returning. If we don't use a Handler at all, the
                // callbacks are already called before we get here. If we do
                // use a Handler but we use the same Looper, waiting here
                // would actually block the callbacks from being called

                synchronized (callbackSync) {
                    while (callbacks > 0) {
                        try {
                            callbackSync.wait();
                        } catch (InterruptedException e) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Are we using a Handler to post callbacks ?
     *
     * @return Handler used ?
     */
    public boolean hasHandler() {
        return (handler != null);
    }


    protected static String[] availableTestCommands = new String[] {
            "echo -BOC-",
            "id"
    };

    /**
     * See if the shell is alive, and if so, check the UID
     *
     * @param stdout          Standard output from running availableTestCommands
     * @param checkForRoot true if we are expecting this shell to be running as
     *                     root
     * @return true on success, false on error
     */
    protected static boolean parseAvailableResult(List<String> stdout, boolean checkForRoot) {
        if (stdout == null) {
            return false;
        }

        // this is only one of many ways this can be done
        boolean echo_seen = false;

        for (String line : stdout) {
            if (line.contains("uid=")) {
                // id command is working, let's see if we are actually root
                return !checkForRoot || line.contains("uid=0");
            } else if (line.contains("-BOC-")) {
                // if we end up here, at least the su command starts some kind
                // of shell, let's hope it has root privileges - no way to know without
                // additional native binaries
                echo_seen = true;
            }
        }

        return echo_seen;
    }

    /**
     * Attempts to deduce if the shell command refers to a su shell
     *
     * @param shell Shell command to run
     * @return Shell command appears to be su
     */
    public static boolean isSU(String shell) {
        // Strip parameters
        int pos = shell.indexOf(' ');
        if (pos >= 0) {
            shell = shell.substring(0, pos);
        }

        // Strip path
        pos = shell.lastIndexOf('/');
        if (pos >= 0) {
            shell = shell.substring(pos + 1);
        }

        return shell.equals("su");
    }

    private interface OnResult {
        // for any onCommandResult callback
        int WATCHDOG_EXIT = -1;
        int SHELL_DIED = -2;

        // for Interactive.open() callbacks only
        int SHELL_EXEC_FAILED = -3;
        int SHELL_WRONG_UID = -4;
        int SHELL_RUNNING = 0;
    }

    /**
     * Command result callback, notifies the recipient of the completion of a
     * command block, including the (last) exit code, and the full output
     */
    public interface OnCommandResultListener extends OnResult {
        /**
         * <p>
         * Command result callback
         * </p>
         * <p>
         * Depending on how and on which thread the shell was created, this
         * callback may be executed on one of the gobbler threads. In that case,
         * it is important the callback returns as quickly as possible, as
         * delays in this callback may pause the native process or even result
         * in a deadlock
         * </p>
         * <p>
         * See {@link Shell} for threading details
         * </p>
         *
         * @param commandCode Value previously supplied to addCommand
         * @param exitCode    Exit code of the last command in the block
         * @param stdout      All output to stdout generated by the command block
         * @param stderr      All output to stderr generated by the command block
         */
        void onCommandResult(int commandCode, int exitCode, List<String> stdout,
                             List<String> stderr);
    }

    /**
     * Command per line callback for parsing the output line by line without
     * buffering It also notifies the recipient of the completion of a command
     * block, including the (last) exit code.
     */
    public interface OnCommandLineListener extends OnResult {
        /**
         * <p>
         * Command result callback
         * </p>
         * <p>
         * Depending on how and on which thread the shell was created, this
         * callback may be executed on one of the gobbler threads. In that case,
         * it is important the callback returns as quickly as possible, as
         * delays in this callback may pause the native process or even result
         * in a deadlock
         * </p>
         * <p>
         * See {@link Shell} for threading details
         * </p>
         *
         * @param commandCode Value previously supplied to addCommand
         * @param exitCode    Exit code of the last command in the block
         */
        void onCommandResult(int commandCode, int exitCode);
        void onStdoutLine(String line);
        void onStderrLine(String line);
    }

    /**
     * Internal class to store command block properties
     */
    private static class Command {
        private static int commandCounter = 0;

        private final String[] commands;
        private final int code;
        private final OnCommandResultListener onCommandResultListener;
        private final OnCommandLineListener onCommandLineListener;
        private final String marker;

        public Command(String[] commands, int code,
                       OnCommandResultListener onCommandResultListener,
                       OnCommandLineListener onCommandLineListener) {
            this.commands = commands;
            this.code = code;
            this.onCommandResultListener = onCommandResultListener;
            this.onCommandLineListener = onCommandLineListener;
            this.marker = UUID.randomUUID().toString() + String.format("-%08x", ++commandCounter);
        }
    }


    /**
     * Builder class for {@link Shell}
     */
    public static class Builder {
        private Handler handler = null;
        private boolean autoHandler = true;
        private String shell = "sh";
        private boolean wantStderr = false;
        private List<Command> commands = new LinkedList<>();
        private Map<String, String> environment = new HashMap<>();
        private StreamGobbler.OnLineListener onStdoutLineListener = null;
        private StreamGobbler.OnLineListener onStderrLineListener = null;
        private int watchdogTimeout = 0;

        /**
         * <p>
         * Set a custom handler that will be used to post all callbacks to
         * </p>
         * <p>
         * See {@link Shell} for further details on threading and
         * handlers
         * </p>
         *
         * @param handler Handler to use
         * @return This Builder object for method chaining
         */
        public Builder setHandler(Handler handler) {
            this.handler = handler;
            return this;
        }

        /**
         * <p>
         * Automatically create a handler if possible ? Default to true
         * </p>
         * <p>
         * See {@link Shell} for further details on threading and
         * handlers
         * </p>
         *
         * @param autoHandler Auto-create handler ?
         * @return This Builder object for method chaining
         */
        public Builder setAutoHandler(boolean autoHandler) {
            this.autoHandler = autoHandler;
            return this;
        }

        /**
         * Set shell binary to use. Usually "sh" or "su", do not use a full path
         * unless you have a good reason to
         *
         * @param shell Shell to use
         * @return This Builder object for method chaining
         */
        public Builder setShell(String shell) {
            this.shell = shell;
            return this;
        }

        /**
         * Convenience function to set "sh" as used shell
         *
         * @return This Builder object for method chaining
         */
        public Builder useSH() {
            return setShell("sh");
        }

        /**
         * Convenience function to set "su" as used shell
         *
         * @return This Builder object for method chaining
         */
        public Builder useSU() {
            return setShell("su");
        }

        /**
         * Set if error output should be appended to command block result output
         *
         * @param wantStderr Want error output ?
         * @return This Builder object for method chaining
         */
        public Builder setWantStderr(boolean wantStderr) {
            this.wantStderr = wantStderr;
            return this;
        }

        /**
         * Add or update an environment variable
         *
         * @param key   Key of the environment variable
         * @param value Value of the environment variable
         * @return This Builder object for method chaining
         */
        public Builder addEnvironment(String key, String value) {
            environment.put(key, value);
            return this;
        }

        /**
         * Add or update environment variables
         *
         * @param addEnvironment Map of environment variables
         * @return This Builder object for method chaining
         */
        public Builder addEnvironment(Map<String, String> addEnvironment) {
            environment.putAll(addEnvironment);
            return this;
        }

        /**
         * <p>
         * Set a callback called for every line output to stdout by the shell
         * </p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Shell} for further details
         * </p>
         *
         * @param onLineListener Callback to be called for each line
         * @return This Builder object for method chaining
         */
        public Builder setOnStdoutLineListener(StreamGobbler.OnLineListener onLineListener) {
            this.onStdoutLineListener = onLineListener;
            return this;
        }

        /**
         * <p>
         * Set a callback called for every line output to stderr by the shell
         * </p>
         * <p>
         * The thread on which the callback executes is dependent on various
         * factors, see {@link Shell} for further details
         * </p>
         *
         * @param onLineListener Callback to be called for each line
         * @return This Builder object for method chaining
         */
        public Builder setOnStderrLineListener(StreamGobbler.OnLineListener onLineListener) {
            this.onStderrLineListener = onLineListener;
            return this;
        }

        /**
         * <p>
         * Enable command timeout callback
         * </p>
         * <p>
         * This will invoke the onCommandResult() callback with exitCode
         * WATCHDOG_EXIT if a command takes longer than watchdogTimeout seconds
         * to complete.
         * </p>
         * <p>
         * If a watchdog timeout occurs, it generally means that the Interactive
         * session is out of sync with the shell process. The caller should
         * close the current session and open a new one.
         * </p>
         *
         * @param watchdogTimeout Timeout, in seconds; 0 to disable
         * @return This Builder object for method chaining
         */
        public Builder setWatchdogTimeout(int watchdogTimeout) {
            this.watchdogTimeout = watchdogTimeout;
            return this;
        }

        /**
         * <p>
         * Enable/disable reduced logcat output
         * </p>
         * <p>
         * Note that this is a global setting
         * </p>
         *
         * @param useMinimal true for reduced output, false for full output
         * @return This Builder object for method chaining
         */
        public Builder setMinimalLogging(boolean useMinimal) {
            Debug.setLogTypeEnabled(Debug.LOG_COMMAND | Debug.LOG_OUTPUT, !useMinimal);
            return this;
        }

        /**
         * Construct a {@link Shell} instance, and start the shell
         *
         * @return Interactive shell
         */
        public Shell open() {
            return new Shell(this, null);
        }

        /**
         * Construct a {@link Shell} instance, try to start the
         * shell, and call onCommandResultListener to report success or failure
         *
         * @param onCommandResultListener Callback to return shell open status
         * @return Interactive shell
         */
        public Shell open(OnCommandResultListener onCommandResultListener) {
            return new Shell(this, onCommandResultListener);
        }
    }
}
