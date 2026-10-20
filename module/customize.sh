#!/system/bin/sh

ui_print "- PillarBox engine v1.0.0"

mkdir -p /data/adb/pillarbox
cp -f "$MODPATH/bin/pillarboxd" /data/adb/pillarbox/pillarboxd
chmod 0755 /data/adb/pillarbox/pillarboxd

if [ ! -f /data/adb/pillarbox/config.json ]; then
  echo '{"enabled":false,"apps":{}}' > /data/adb/pillarbox/config.json
fi
chmod 0644 /data/adb/pillarbox/config.json

set_perm_recursive "$MODPATH" 0 0 0755 0644
set_perm "$MODPATH/bin/pillarboxd" 0 0 0755
set_perm "$MODPATH/service.sh" 0 0 0755
set_perm "$MODPATH/uninstall.sh" 0 0 0755
set_perm "$MODPATH/customize.sh" 0 0 0755

ui_print "- Engine installed to /data/adb/pillarbox"
ui_print "- Install the PillarBox app, then pick your games"
