#!/bin/bash
set -e

for dpi in {m,h,xh,xxh,xxxh}dpi; do
    cp "launcher_icon-${dpi}.png" "../app/src/main/res/mipmap-${dpi}/launcher_icon.png"
    cp "launcher_icon_foreground-${dpi}.png" "../app/src/main/res/mipmap-${dpi}/launcher_icon_foreground.png"
done
cp launcher_icon-web.png ../app/src/main/launcher_icon-web.png

for dpi in {m,h,xh,xxh,xxxh}dpi; do
    cp "ftp_server_shortcut_icon-${dpi}.png" "../app/src/main/res/mipmap-${dpi}/ftp_server_shortcut_icon.png"
done
