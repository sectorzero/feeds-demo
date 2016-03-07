# Feeds Demo

# Design & Reasoning
* Pending Documentation - Some core thoughts here.

# To Execute
* Download single fat-jar [feeds.jar](https://www.dropbox.com/s/8ziwcqxl8n94mcg/feeds-logback.xml)
* Download the config file [feeds.yml](https://www.dropbox.com/s/x833knqpojkmunj/feeds.yml)
* Execute 
```
java -jar ./feeds.jar server ./feeds.yml
```
* http REST API 
	* available @ port 10991
	* /swagger shows UI to the APIs
* Requires
	* /tmp to be writable - Kafka and Zookeeper logs generated in /tmp/feeds-demo
	* Ports 10991, 10992 and 10993 need to be opened for listening
	* ./ needs to be writable for applcation logs 

# API Demo
* /swagger shows an UI to the APIs

## /feeds
* Lists the feeds available in the system
* For the demo, the system comes with a preconfigured set of feeds from 'f_1' ... 'f_8'

## /feeds/{feedname}
* Shows all the article-refs posted to the feed

## /feeds/submit/{feedname}?articleRefId=<some_ref_id_string>
* For simplicity for the demo and due to time constraints, I did not implement a store and functionality to take in urls and generate a ref-id. Instead, please assume that you work only with the generated ids for the article. The articleRefId is supposed to be some string id generated for the url to be bpublished. Since most of the interesting part happens with the refId, I have done it this way

## /aggregate?feedname=<x>&feedname=<y>&...
* Note on subscriptions. Subscriptions is a set of feeds and needs update APIs provided to manipulate them. But I could not get this minor functionality implemented. I intended to put these in a key-value store, but could not get them in. Instead I have made the API itself take in the set of feednames and will aggregate the list of articles from those feeds.
* Eg.
```http://localhost:10991/aggregate?feename=f_1&feedname=f_5&feedname=f_6&numEntries=10
```

# Known Issues
* The simplified implementation only stores and shows only last N entries in some configured number.
* When the application restarts, the Kakfa Consumers take about 30-40 seconds to start reading back from the feed persistent queues. I could not get to the the bottom of this. So when the appl re-starts, it will take about 30-40 seconds for the feed articles to show up
* Lot of things are simplied and some APIs are not implemented which are purely key-value lookups. But I have implemed the core article submission to the feed and aggregation of articles from the set of feeds functionality
* I have put in some thought into the design and I wanted to do a brief write-up, but I could not complete it. I will add it in shortly.
* Currently when the application starts up, I implemented such that only the last n entries in the feed queue need to be re-read, but that did not work well with as Kafka had some conditions which I do not understand well, so when the appl restarts, the consumers re-read all the messages in the feed-queue from beginning, but store only the last N for aggregation.