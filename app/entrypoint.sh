#!/bin/bash
if [ ! "$(ls -A /home/${APP_USER}/resources/knowledge_base)" ]; then
  cp -r /app/resources/knowledge_base /home/${APP_USER}/resources/
fi
exec "$@"
