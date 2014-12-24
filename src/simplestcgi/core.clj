(ns simplestcgi.core
 (:use [org.httpkit.server :only [run-server]])
 (:require [com.palletops.docker :refer :all]
           [clojure.string :as string]))

(defn app [req]
  (let [strHostIp (System/getenv "HOST_IP")
        strHostId (System/getenv "HOST_ID")]
    (if (or (empty? strHostIp) (empty? strHostId))
      {:status  200
       :headers {"Content-Type" "text/html"}
       :body    (str "<DIV STYLE=\"font-family: Consolas, Menlo, 'Liberation Mono', Courier, monospace;\">"
                     "Please set env params!<br/>"
                     strHostIp "<br/>"
                     strHostId "<br/>"
                     "</DIV>")}
      (let [objContainer (docker (str "http://" strHostIp ":4243") {:command :container-processes :id strHostId})]
        (println strHostIp "<br/>" strHostId "<br/>")
        {:status  200
         :headers {"Content-Type" "text/html"}
         :body    (str "<DIV STYLE=\"font-family: Consolas, Menlo, 'Liberation Mono', Courier, monospace;\">"
                       objContainer
                       "</DIV>")}
        ))))

(defn -main [& args]
  (let [port 80]
    (run-server app {:port port})
    (println (str "Server started. listen at localhost@" port))))
