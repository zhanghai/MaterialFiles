#!/bin/bash
set -e
inkscape -e launcher_icon-mdpi.png -w 48 -h 48 launcher_icon.svg
inkscape -e launcher_icon-hdpi.png -w 72 -h 72 launcher_icon.svg
inkscape -e launcher_icon-xhdpi.png -w 96 -h 96 launcher_icon.svg
inkscape -e launcher_icon-xxhdpi.png -w 144 -h 144 launcher_icon.svg
inkscape -e launcher_icon-xxxhdpi.png -w 192 -h 192 launcher_icon.svg
inkscape -e launcher_icon-web.png -D -w 512 -h 512 launcher_icon.svg

cp launcher_icon.svg launcher_icon_foreground.svg
inkscape --select=circle-clip-group --verb=SelectionUnGroup --verb=EditDeselect --select=circle --select=circle-edge-top --select=circle-edge-bottom --verb=EditDelete --verb=FileSave --verb=FileQuit launcher_icon_foreground.svg
inkscape -e launcher_icon_foreground-mdpi.png -w 108 -h 108 launcher_icon_foreground.svg
inkscape -e launcher_icon_foreground-hdpi.png -w 162 -h 162 launcher_icon_foreground.svg
inkscape -e launcher_icon_foreground-xhdpi.png -w 216 -h 216 launcher_icon_foreground.svg
inkscape -e launcher_icon_foreground-xxhdpi.png -w 324 -h 324 launcher_icon_foreground.svg
inkscape -e launcher_icon_foreground-xxxhdpi.png -w 432 -h 432 launcher_icon_foreground.svg
inkscape -e launcher_icon-play.png -a 18:18:90:90 -b '#1976d2' -w 512 -h 512 launcher_icon_foreground.svg
rm launcher_icon_foreground.svg
