#!/bin/bash
set -e

inkscape -o launcher_icon-mdpi.png --export-area=15:15:93:93 -w 48 -h 48 launcher_icon.svg
inkscape -o launcher_icon-hdpi.png --export-area=15:15:93:93 -w 72 -h 72 launcher_icon.svg
inkscape -o launcher_icon-xhdpi.png --export-area=15:15:93:93 -w 96 -h 96 launcher_icon.svg
inkscape -o launcher_icon-xxhdpi.png --export-area=15:15:93:93 -w 144 -h 144 launcher_icon.svg
inkscape -o launcher_icon-xxxhdpi.png --export-area=15:15:93:93 -w 192 -h 192 launcher_icon.svg

cp launcher_icon.svg launcher_icon-web.svg
inkscape --batch-process --actions='select:circle;RemoveFilter;FileSave;FileQuit' launcher_icon-web.svg
inkscape -o launcher_icon-web.png --export-area=18:18:90:90 -w 512 -h 512 launcher_icon-web.svg
rm launcher_icon-web.svg

cp launcher_icon.svg launcher_icon_foreground.svg
inkscape --batch-process --actions='select:circle;select:circle-edge-top;select:circle-edge-bottom;EditDelete;select:circle-clip-group;SelectionUnGroup;FileSave;FileQuit' launcher_icon_foreground.svg
inkscape -o launcher_icon_foreground-mdpi.png -w 108 -h 108 launcher_icon_foreground.svg
inkscape -o launcher_icon_foreground-hdpi.png -w 162 -h 162 launcher_icon_foreground.svg
inkscape -o launcher_icon_foreground-xhdpi.png -w 216 -h 216 launcher_icon_foreground.svg
inkscape -o launcher_icon_foreground-xxhdpi.png -w 324 -h 324 launcher_icon_foreground.svg
inkscape -o launcher_icon_foreground-xxxhdpi.png -w 432 -h 432 launcher_icon_foreground.svg
inkscape -o launcher_icon-play.png --export-area=18:18:90:90 -b '#1a73e8' -w 512 -h 512 launcher_icon_foreground.svg
rm launcher_icon_foreground.svg

for shortcut in directory downloads file ftp_server; do
    inkscape -o "${shortcut}_shortcut_icon-mdpi.png" --export-area=15:15:93:93 -w 48 -h 48 "${shortcut}_shortcut_icon.svg"
    inkscape -o "${shortcut}_shortcut_icon-hdpi.png" --export-area=15:15:93:93 -w 72 -h 72 "${shortcut}_shortcut_icon.svg"
    inkscape -o "${shortcut}_shortcut_icon-xhdpi.png" --export-area=15:15:93:93 -w 96 -h 96 "${shortcut}_shortcut_icon.svg"
    inkscape -o "${shortcut}_shortcut_icon-xxhdpi.png" --export-area=15:15:93:93 -w 144 -h 144 "${shortcut}_shortcut_icon.svg"
    inkscape -o "${shortcut}_shortcut_icon-xxxhdpi.png" --export-area=15:15:93:93 -w 192 -h 192 "${shortcut}_shortcut_icon.svg"
done

inkscape -o banner-xhdpi.png --export-area=0:0:320:180 -w 320 -h 180 banner.svg
inkscape -o banner-play.png --export-area=0:0:320:180 -w 1280 -h 720 banner.svg
