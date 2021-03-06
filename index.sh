#!/bin/bash

# NUTCH=./bin/nutch
NUTCH=/root/nutch/bin/nutch
WEBTABLE=webtable
INDEX_PATH=/root/index

echo "Indexing data from '$WEBTABLE' to $INDEX_PATH..."
if [[ -e $INDEX_PATH ]]; then
    echo "Deleted the old index."
    rm -r $INDEX_PATH
fi

$NUTCH org.apache.nutchbase.indexer.IndexerHbase $INDEX_PATH $WEBTABLE
echo "done."
