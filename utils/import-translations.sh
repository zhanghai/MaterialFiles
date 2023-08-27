#!/bin/bash
set -e

LOCALES=(
    ar
    bg
    cs
    de
    el
    es
    eu
    fa
    fi
    fr
    hu
    in
    is
    it
    iw
    ja
    ko
    lt
    nb
    nl
    pl
    pt-rBR
    pt-rPT
    ro
    ru
    tr
    uk
    vi
)

for zip_file in "$@"; do
    resource="$(basename "${zip_file}" .zip | sed -E 's/zhanghai_materialfiles_(.*)xml/\1/')"
    for locale in "${LOCALES[@]}"; do
        values_directory="../app/src/main/res/values-${locale}"
        mkdir -p "${values_directory}"
        transifex_locale="$(echo "${locale}" | sed -e 's/-r/_/' -e 's/iw/he/' -e 's/ji/yi/' -e 's/in/id/')"
        unzip -p "${zip_file}" "${resource}xml_${transifex_locale}.xml" | perl -p0e 's/\n    (<\/resources>)/\1/' >"${values_directory}/${resource}.xml"
    done
done
