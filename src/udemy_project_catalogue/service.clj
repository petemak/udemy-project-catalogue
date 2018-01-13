(ns udemy-project-catalogue.service
  (:require [io.pedestal.http :as pdstl-http]
            [io.pedestal.http.route :as pdstl-route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.interceptor.helpers :refer [definterceptor defhandler]]
            [ring.util.response :as ring-resp]
            [clj-http.client :as http-client]
            [udemy-project-catalogue.db :as db]
            [clojure.data.json :as json]
            [clojure.data.xml :as xml]))


;; Just for testing
(def security-token (System/getenv "SECURITY_TOKEN"))


(def raw-prj-xml
  "<project>
     <proj-name>Amharigna</proj-name>
     <name>Amarigna Tichalesh?</name>
     <name>ALearn Amharic</name>
     <framework>re-frame</framework>
     <language>Clojure</language>
     <repo>https://gitlab.com/pemak/projects</repo>
   </project>")

(def project-xml (xml/parse-str raw-prj-xml))


(defn get-by-tag
  "Extract value of a tag"
  [proj-map tname]
  (->> proj-map
       :content
       (filter #(= (:tag %) tname))
       first
       :content
       first))



(defn auth0-token
  "Post to Aurh"
  []
  (let [ret (http-client/post "https://petemak.eu.auth0.com/oauth/token"
                              {:debug false
                               :content-type :json
                               :form-params {:client_id (System/getenv "AUTH0_UID")
                                             :client_secret (System/getenv "AUTH0_PWD")
                                             :grant_type "client_credentials"}})]
    (json/read-str (:body ret))))


(defn auth0-api-call
  "Calls Auth0 API using specified token"
  [token]
  (let [ret (http-client/get "https://petemak.eu.auth0.com/api/connections"
                             {:debug false
                              :content-type :json
                              :accept :json
                              :headers {"Authorization" (format "Bearer %s" token)}})]))



(defn dump-request
  "Dump request object"
  [request]
  (prn "=====================================")
  (prn ":json-params")
  (prn (:json-params request))
  (prn "\n")
  (prn "Whole request:")
  (prn request)
  (prn "====================================="))


(defhandler token-checker
  "Simple dev implementtion: compares submitted toke against the known one"
  [request]
  (let [token (get-in request [:headers "x-catalogue-token"])]
    (if (not (= token security-token))
      (assoc (ring-resp/response {:body "access denied"}) :status 403))))




(defn xmldata-to-map
  "Take a raw project XML data and map into a simple map"
  [xml-str]
  (let [proj-xml (xml/parse-str xml-str)]
    {:proj-name (get-by-tag  proj-xml :proj-name)
     :name (get-by-tag proj-xml :name)
     :framework (get-by-tag proj-xml :name)
     :language (get-by-tag proj-xml :language)
     :repo (get-by-tag proj-xml :repo)}))



(defn map-to-xmldata
  "Turns a project map object into raw xml"
  [src-map]
  (xml/element :project {}
               (xml/element :id {} (.toString (:_id src-map)))
               (xml/element :proj-name {} (:proj-name src-map))
               (xml/element :name {} (:name src-map))
               (xml/element :framework {} (:framework src-map))
               (xml/element :language {} (:language src-map))
               (xml/element :repo {} (:repo src-map))))


(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (pdstl-route/url-for ::about-page))))

(defn home-page
  [request]
  (ring-resp/response "===== Hello from Udemy Catalogue Servive sample app 2! ====="))

(defn home-page2
  [request]
  (let [projects (db/get-projects)]
    (pdstl-http/json-response projects)))



(defn add-project
  "Handlee service for adding projects"
  [request]
  (let [to-add (:json-params request)
        results (db/insert-project to-add)]
    (ring-resp/created
      "http://dummy-url-for-inserts"
      results)))



(defn add-project-xml
  [request]
  (let [incoming-data (slurp (:body request))
        proj-map (xmldata-to-map incoming-data)
        ret (db/insert-project proj-map)]
    (-> (ring-resp/created "http://dummy-url-for-inserts"
                           (xml/emit-str (map-to-xmldata ret)))
        (ring-resp/content-type "application/xml"))))


(defn get-projects
  "Services the request for projects. Returns a collection of all
   registerred projects"
  [request]
  (let [projects (db/get-projects)]
    (pdstl-http/json-response projects)))


(defn get-project
  "Services the request for specific project. Returns a project
  of the name specified as a path parameter e.g as 'sleeping-cat' in /projects/sleeping-cat"
  [request]
  (let [proj-name (get-in request [:path-params :project-name])
        proj (db/get-project proj-name)]
    (pdstl-http/json-response proj)))


(defn github-search
  [q]
  (let [url (format "https://api.github.com/search/repositories?q=%s+language:clojure" q)
        ret (http-client/get url
                             {:debug false
                              :content-type :json
                              :accept :json})]
    (json/read-str (:body ret))))


(defn git-get
  "retrieves the querry parameter which is mapped to \"q\"
   and calls (github-search).
   \"q\" represents the paraemter after ? in the URL"
  [request]
  (let [search-term (get-in request [:query-params :q])]
    (pdstl-http/json-response (github-search search-term))))




;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) pdstl-http/html-body token-checker])

;; Tabular routes
(def routes #{["/" :get (conj common-interceptors `home-page2)]
              ["/about" :get (conj common-interceptors `about-page)]
              ["/projects" :get (conj common-interceptors `get-projects)]
              ["/projects/:project-name" :get (conj common-interceptors `get-project)]
              ["/projects" :post (conj common-interceptors `add-project)]
              ["/projects-xml" :post (conj common-interceptors `add-project-xml)]
              ["/see-also" :get (conj common-interceptors `git-get)]})

;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by udemy-project-catalogue.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::pdstl-http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::pdstl-http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::pdstl-http/type  :jetty
              ;;::http/host "localhost"
              ::pdstl-http/port (Integer. (or (System/getenv "PORT") 7070))
              ;; Options to pass to the container (Jetty)
              ::pdstl-http/container-options {:h2c? true
                                              :h2? false
                                              ;:keystore "test/hp/keystore.jks"
                                              ;:key-password "password"
                                              ;:ssl-port 8443
                                              :ssl? false}})

