#!/system/bin/sh

/data/adb/pillarbox/pillarboxd stop >/dev/null 2>&1
wm size reset >/dev/null 2>&1
wm density reset >/dev/null 2>&1
rm -rf /data/adb/pillarbox
