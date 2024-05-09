#!/usr/bin/bash

# Get the broker and properties file from command line arguments
kafka_broker=$1
properties_file=$2

# Array of required topics
required_topics=("images.wip" "images.done")  # Update with your actual topics

# Check each topic in the array
for topic in "${required_topics[@]}"; do
  echo "Checking for topic: $topic"
  # Use kafka-topics.sh to list topics with the properties file
  if ! kafka-topics --bootstrap-server $kafka_broker --list --command-config $properties_file | grep -w $topic > /dev/null; then
    echo "Topic $topic not found."
    exit 1
  fi
done

echo "All required topics are present."
exit 0
