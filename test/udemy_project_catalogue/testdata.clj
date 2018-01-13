(ns udemy-project-catalogue.testdata
  (:require [clojure.test :refer :all]))


;; Example of the request map passed to a pedestal interceptor defined
;; in the routes
(def pdstl-request {:json-params {:name "Rogger Rabit",
                                  :framework "Toon Anthropomorphic",
                                  :language "Hyperactive friendly talkative funny lingua",
                                  :repo "https://gitlab.com/pemak/projects/rogerrabit"},
                    :protocol "HTTP/1.1",
                    :async-supported? true,
                    :remote-addr "0:0:0:0:0:0:0:1",
                    :servlet-response #object[org.eclipse.jetty.server.Response 0x50ea6fe3 "HTTP/1.1 200 \nDate: Sat, 06 Jan 2018 09:40:22 GMT\r\n\r\n"],
                    :servlet #object[io.pedestal.http.servlet.FnServlet 0x15e8eb9a "io.pedestal.http.servlet.FnServlet@15e8eb9a"],
                    :headers {"origin" "chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop",
                              "host" "localhost:7070",
                              "user-agent" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36",
                              "content-type" "application/json",
                              "content-length" "194",
                              "connection" "keep-alive",
                              "accept" "*/*", "accept-language" "en-US,en;q=0.9,de;q=0.8,ru;q=0.7",
                              "accept-encoding" "gzip, deflate, br",
                              "postman-token" "bfc64a31-79c6-9f0d-a325-ab0827b171f6",
                              "dnt" "1", "cache-control" "no-cache"},
                    :server-port 7070,
                    :servlet-request #object[org.eclipse.jetty.server.Request 0x5f944328 "Request(POST //localhost:7070/projects)@5f944328"],
                    :content-length 194,
                    :content-type "application/json",
                    :path-info "/projects",
                    :character-encoding "UTF-8",
                    :url-for #object[clojure.lang.Delay 0x27218f90 {:status :pending, :val nil}],
                    :uri "/projects",
                    :server-name "localhost",
                    :query-string nil,
                    :path-params {},
                    :body #object[org.eclipse.jetty.server.HttpInputOverHTTP 0x3c25474e "HttpInputOverHTTP@3c25474e[c=194,q=0,[0]=null,s=STREAM]"],
                    :scheme :http,
                    :request-method :post})


