#!/bin/bash
set -euo pipefail

currentTimestamp() {
  date -u +"%Y-%m-%dT%H:%M:%S.%3NZ"
}

est_message=$(cat <<EOF
{
  "message": {
    "content":"Virtuaalne vestlusrobot ärkab sõnade kaudu ellu, jäljendades inimlikku rütmi ja tooni. Tehisintellekti loodud vastused voolavad katkestusteta, justkui sulanduksid need päris vestluse öö voogu.",
    "authorTimestamp":"$(currentTimestamp)",
    "authorRole":"end-user"
    },
  "endUserTechnicalData": {
    "endUserUrl":"http://localhost:3003/",
    "endUserOs":"Agent: Firefox (v143.0), OS: Ubuntu (vnone), device: unknown"
    },
  "holidays": [
    "2025-01-01",
    "2025-02-24",
    "2025-04-18",
    "2025-04-20",
    "2025-05-01",
    "2025-06-08",
    "2025-06-23",
    "2025-06-24",
    "2025-08-20",
    "2025-12-24",
    "2025-12-25",
    "2025-12-26"
    ],
  "holidayNames": "2025-01-01-uusaasta,2025-02-24-iseseisvuspäev,2025-04-18-suur reede,2025-04-20-lihavõtted,2025-05-01-kevadpüha,2025-06-08-nelipühade 1. püha,2025-06-23-võidupüha,2025-06-24-jaanipäev,2025-08-20-taasiseseisvumispäev,2025-12-24-jõululaupäev,2025-12-25-esimene jõulupüha,2025-12-26-teine jõulupüha",
  "domain":"none"
}
EOF
)


script_name=`basename $0`
pwd
echo "$(currentTimestamp) - $script_name started"

echo "$(currentTimestamp) - Creating new chat"

response=$(curl -s -X POST "http://localhost:8086/backoffice/chats/init" \
  -H "Content-Type: application/json" \
  -d "$est_message")

chat_id=$(echo "$response" | jq -r '.response.id')


if [[ -z "$chat_id" ]]; then
  echo "Failed to get chat_id from response"
  exit 1
fi

eng_message=$(cat <<EOF
{
  "message": {
    "chatId":"$chat_id",
    "content":"Farewell echoes drift like soft whispers across a fading horizon, where moments dissolve into gentle silence. In the pause between parting and memory, the words linger, shaping the quiet of what comes next.",
    "authorTimestamp":"$(currentTimestamp)",
    "authorRole":"end-user"
    },
  "holidays": [
    "2025-01-01",
    "2025-02-24",
    "2025-04-18",
    "2025-04-20",
    "2025-05-01",
    "2025-06-08",
    "2025-06-23",
    "2025-06-24",
    "2025-08-20",
    "2025-12-24",
    "2025-12-25",
    "2025-12-26"
    ],
  "holidayNames":"2025-01-01-uusaasta,2025-02-24-iseseisvuspäev,2025-04-18-suur reede,2025-04-20-lihavõtted,2025-05-01-kevadpüha,2025-06-08-nelipühade 1. püha,2025-06-23-võidupüha,2025-06-24-jaanipäev,2025-08-20-taasiseseisvumispäev,2025-12-24-jõululaupäev,2025-12-25-esimene jõulupüha,2025-12-26-teine jõulupüha",
  "domain":"none"
}
EOF
)

sleep 1.5

response=$(curl -s -X POST "http://localhost:8086/backoffice/chats/messages/add" \
  -H "Content-Type: application/json" \
  -d "$eng_message")

sleep 1.5

terminate_message=$(cat <<EOF
{
  "message": {
    "chatId":"$chat_id",
    "authorTimestamp":"$(currentTimestamp)",
    "authorRole":"end-user",
    "event": "client_left_for_unknown_reasons"
    },
  "status": "ENDED"
  "domain":"none"
}
EOF
)

terminateChat=$(curl -s -X POST "http://localhost:8086/backoffice/chats/end" \
      -H "Content-Type: application/json" \
      -d "$terminate_message")

echo "$(currentTimestamp) - $script_name finished, chat with id $chat_id was created and terminated"
