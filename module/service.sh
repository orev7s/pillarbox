#!/system/bin/sh

MODDIR=${0%/*}

sleep 8

if [ ! -x /data/adb/pillarbox/pillarboxd ]; then
  mkdir -p /data/adb/pillarbox
  cp -f "$MODDIR/bin/pillarboxd" /data/adb/pillarbox/pillarboxd
  chmod 0755 /data/adb/pillarbox/pillarboxd
fi

/data/adb/pillarbox/pillarboxd start
