/**
 * Output format:
 * - File:
 *     - OK: mode(unsigned integer) NULL owner_id(unsigned integer) NULL group_id(unsigned integer)
 *     NULL size(long) last_modification_time_seconds(long) NULL
 *     last_modification_time_nanoseconds(long) NULL is_symbolic_link(boolean) ( NULL
 *     symbolic_link_target(string) )? NULL has_owner_name(boolean) ( NULL owner_name(string) )?
 *     NULL has_group_name(boolean) ( NULL group_name(string) )?
 *     - Error: (empty)
 * - Directory:
 *     - OK: name file_output? ( NULL NULL NULL \n NULL NULL NULL name file_output? )+
 *     - Error: (empty)
 */

#include <errno.h>
#include <stdbool.h>
#include <stdlib.h>
#include <string.h>

#include <dirent.h>
#include <grp.h>
#include <pwd.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>

void print_field_boolean(bool field) {
    fputc(field ? '1' : '0', stdout);
}

void print_field_long(long field) {
    fprintf(stdout, "%ld", field);
}

void print_field_string(char *field) {
    fputs(field, stdout);
}

void print_field_unsigned_integer(unsigned int field) {
    fprintf(stdout, "%u", field);
}

void print_field_seperator() {
    fputc('\0', stdout);
}

void print_record_separator() {
    // Can only fail when two empty fields are followed by a newline field and then two empty
    // fields.
    // To guard against this, don't output two consecutive possibly empty fields.
    fputc('\0', stdout);
    fputc('\0', stdout);
    fputc('\0', stdout);
    fputc('\n', stdout);
    fputc('\0', stdout);
    fputc('\0', stdout);
    fputc('\0', stdout);
}

int print_file(char *path) {

    struct stat64 lstat;
    errno = 0;
    lstat64(path, &lstat);
    if (errno) {
        return errno;
    }

    bool is_symbolic_link = S_ISLNK(lstat.st_mode);
    char *symbolic_link_target = NULL;
    bool symbolic_link_has_stat = false;
    struct stat64 symbolic_link_stat;
    if (is_symbolic_link) {

        // TODO: Bad file size?
        size_t symbolic_link_target_buffer_size = (size_t) lstat.st_size + 1;
        symbolic_link_target = malloc(symbolic_link_target_buffer_size);
        errno = 0;
        ssize_t symbolic_link_target_size = readlink(path, symbolic_link_target,
                                                     symbolic_link_target_buffer_size);
        if (errno) {
            free(symbolic_link_target);
            return errno;
        }
        if (symbolic_link_target_size >= symbolic_link_target_buffer_size) {
            symbolic_link_target_size = symbolic_link_target_buffer_size - 1;
        }
        symbolic_link_target[symbolic_link_target_size] = '\0';

        errno = 0;
        stat64(path, &symbolic_link_stat);
        if (!errno) {
            symbolic_link_has_stat = true;
        }
    }

    struct stat64 *stat = symbolic_link_has_stat ? &symbolic_link_stat : &lstat;

    // TODO: Use getpwuid_r()?
    char *owner_name = NULL;
    struct passwd *passwd = getpwuid(stat->st_uid);
    if (passwd) {
        owner_name = passwd->pw_name;
    }

    // TODO: Use getgrgid_r()?
    char *group_name = NULL;
    struct group *group = getgrgid(stat->st_gid);
    if (group) {
        group_name = group->gr_name;
    }

    print_field_unsigned_integer(stat->st_mode);
    print_field_seperator();
    print_field_unsigned_integer(stat->st_uid);
    print_field_seperator();
    print_field_unsigned_integer(stat->st_gid);
    print_field_seperator();
    print_field_long(stat->st_size);
    print_field_seperator();
    print_field_long(stat->st_mtim.tv_sec);
    print_field_seperator();
    print_field_long(stat->st_mtim.tv_nsec);
    print_field_seperator();
    print_field_boolean(is_symbolic_link);
    if (is_symbolic_link) {
        print_field_seperator();
        print_field_string(symbolic_link_target);
        free(symbolic_link_target);
    }
    bool has_owner_name = owner_name != NULL;
    print_field_seperator();
    print_field_boolean(has_owner_name);
    if (has_owner_name) {
        print_field_seperator();
        print_field_string(owner_name);
    }
    bool has_group_name = group_name != NULL;
    print_field_seperator();
    print_field_boolean(has_group_name);
    if (has_group_name) {
        print_field_seperator();
        print_field_string(group_name);
    }
    return EXIT_SUCCESS;
}

int print_directory(char *path) {

    errno = 0;
    DIR *dir = opendir(path);
    if (errno) {
        return errno;
    }

    struct dirent64 *dirent;
    while ((dirent = readdir64(dir))) {

        char *file_name = dirent->d_name;
        if (!strcmp(file_name, ".") || !strcmp(file_name, "..")) {
            continue;
        }

        print_field_string(file_name);

        errno = 0;
        char *file_path = malloc(strlen(path) + strlen(file_name) + 2);
        if (errno) {
            print_record_separator();
            continue;
        }
        strcpy(file_path, path);
        strcat(file_path, "/");
        strcat(file_path, file_name);

        print_field_seperator();
        print_file(file_path);
        free(file_path);

        print_record_separator();
    }

    return EXIT_SUCCESS;
}

int main(int argc, char **argv) {
    if (argc != 3) {
        fprintf(stderr, "Invalid argument\n");
        return EXIT_FAILURE;
    }
    if (!strcmp(argv[1], "-f")) {
        return print_file(argv[2]);
    } else if (!strcmp(argv[1], "-d")) {
        return print_directory(argv[2]);
    } else {
        fprintf(stderr, "Invalid first argument, expected -f or -d\n");
        return EXIT_FAILURE;
    }
}
