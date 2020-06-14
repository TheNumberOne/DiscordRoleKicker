#!/bin/sh

set -euf

if [ $# -ne 3 ]; then
  cat <<END
  Please specify three arguments where the first argument is the name of the container,
  the second argument is the name of the image to upgrade to,
  and the third argument is the name of the container to create.
END
  exit
fi

if ! docker inspect "$1" > /dev/null 2>&1; then
  echo "Container $1 doesn't exist."
  exit
fi

if [ "$(docker inspect "$1" -f "{{.State.Running}}")" != "false" ]; then
  echo "Container $1 should not be running"
  exit
fi

token_env=$(docker inspect "$1" -f "{{range .Config.Env}}{{.}}
{{end}}" | grep discord.bot.token)

db_file=$(mktemp)
docker cp "$1:/bot_state.mv.db" "$db_file"
if [ ! -s "$db_file" ]; then
  echo "Database file is empty or hasn't been created. Has the bot been ran?"
fi

container_id=$(docker create --restart=always --env "$token_env" --memory=250m --name="$3" "$2")
docker cp "$db_file" "$3:/bot_state.mv.db"
echo "Container $3 has been created with id $container_id."
echo "To run container, execute \`docker start $3\`"