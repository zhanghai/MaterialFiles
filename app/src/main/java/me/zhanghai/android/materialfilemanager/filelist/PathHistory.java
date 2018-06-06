/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialfilemanager.filelist;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.zhanghai.android.materialfilemanager.filesystem.File;
import me.zhanghai.android.materialfilemanager.functional.Functional;
import me.zhanghai.android.materialfilemanager.util.CollectionUtils;

public class PathHistory {

    private List<Path> mHistory = new ArrayList<>();

    public void push(List<Segment> segments) {
        Path lastPath = getCurrent();
        List<Segment> lastSegments = lastPath != null ? lastPath.segments : null;
        boolean isPrefix = lastSegments != null && segments.size() <= lastSegments.size()
                && Functional.every(segments, (segment, index) -> segment.equals(lastSegments.get(
                        index)));
        Path path = new Path(isPrefix ? lastSegments : segments, segments.size() - 1);
        CollectionUtils.push(mHistory, path);
    }

    public boolean goBack() {
        if (mHistory.size() <= 1) {
            return false;
        }
        CollectionUtils.pop(mHistory);
        return true;
    }

    public Path getCurrent() {
        return CollectionUtils.lastOrNull(mHistory);
    }

    public File getCurrentFile() {
        Path path = getCurrent();
        return path.segments.get(path.index).file;
    }

    public File getFileAt(int index) {
        return getCurrent().segments.get(index).file;
    }

    public static class Path {

        public List<Segment> segments;
        public int index;

        public Path(List<Segment> segments, int index) {
            this.segments = segments;
            this.index = index;
        }
    }

    public static class Segment {

        public String title;
        public File file;

        public Segment(String title, File file) {
            this.title = title;
            this.file = file;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || getClass() != object.getClass()) {
                return false;
            }
            Segment that = (Segment) object;
            return Objects.equals(title, that.title) && Objects.equals(file, that.file);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, file);
        }
    }
}
