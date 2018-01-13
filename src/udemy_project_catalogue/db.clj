(ns udemy-project-catalogue.db
  (:require [monger.core :as mgr-core]
            [monger.collection :as mgr-collection]
            [monger.json :as mgr-json]))

;; Name of collection for project catalogue in Mongo DB
;;
(def project-db-collection "project-catalogue")

(def mock-project-collection {:sleeping-cat {:name "Garfield"
                                             :framework "Food"
                                             :language "Cattalk Lazy"
                                             :repo "https://gitlab.com/pemak/projects/garfield"}
                              :brown-fox {:name "Brown Fox"
                                          :framework "Quick"
                                          :language "Fox talk"
                                          :repo "https://gitlab.com/pemak/projects/brownfox"}
                              :duffy-duck {:name "Duffy Duck"
                                           :framework "Unrestrained"
                                           :language "Slobbery exaggerated lisp"
                                           :repo "https://gitlab.com/pemak/projects/duffyduck"}})

;; Mongo database URL has the form
;; mongodb://<dbuser>:<dbpassword>@ds135156.mlab.com:35156/dev
;; Will be pullled from environment variables
;; project-catalogue
;; - URI: mongodb://<dbuser>:<dbpassword>@ds135156.mlab.com:35156/dev
(defn connect-to-mongo-db
  []
  (let [uri (System/getenv "MONGODB_URI")]
    (mgr-core/connect-via-uri uri)))

(defn get-project
  "Reads and returns a project by the specified name projects from DB.
   The collection is 'project-db-collection'"
  [proj-name]
  (let [{:keys [conn db]} (connect-to-mongo-db)]
    (mgr-collection/find-maps db project-db-collection {:name proj-name})))


(defn get-projects
  "Reads all projects from DB. The collection is 'project-db-collection'"
  []
  (let [{:keys [conn db]} (connect-to-mongo-db)]
    (mgr-collection/find-maps db project-db-collection)))


(defn insert-project
  "inserts a project to the project catalogue stored in the collection project-db-collection"
  [proj-to-add]
  (let [{:keys [conn db]} (connect-to-mongo-db)]
    (mgr-collection/insert-and-return db project-db-collection proj-to-add)))