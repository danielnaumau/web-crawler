## Web crawler 

That's the simple API based app that takes a list of URLs, crawls the data concurrently, and returns the crawled data.
The app is hosted here: **web-crawler.clevelheart.com**

### Api details

> Request
```
endpoint: /api/crawl
http method: post
request body:
```
```json 
 {
   "urls": ["https://google.com", "https://github.com", "https://failed_request.com"]
 } 
```
> Response

```json
{
  "results": [
    {
      "url": "https://google.com", 
      "data": "..."
    }, 
    {
      "url": "https://github.com",
      "data": "..."
    }
  ],
  "errors": [
    "msg": "...",
    "url": "https://failed_request.com"
  ]
}
```

###
Execute **sbt run** to run this application locally. It'll be available here: **localhost:8080**
