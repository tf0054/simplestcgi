(ns simplestcgi.core
 (:use [org.httpkit.server :only [run-server]]
       [clojure.java.shell :only [sh]])
 (:require [clojure.string :as string]))
(defn app [req]
  (let [strHostIp (System/getenv "HOST_IP")]
    (if (empty? strHostIp)
      {:status  200
       :headers {"Content-Type" "text/html"}
       :body    (str "<DIV STYLE=\"font-family: Consolas, Menlo, 'Liberation Mono', Courier, monospace;\">"
                     "Please set env params!<br/>"
                     strHostIp "<br/>"
                     "</DIV>")}
      {:status  200
       :headers {"Content-Type" "text/html"}
       :body    (str "<DIV STYLE=\"font-family: Consolas, Menlo, 'Liberation Mono', Courier, monospace;\">"
                     (sh (str "/usr/bin/docker -H tcp://" strHostIp ":4243 " "ps"))
                     "</DIV>")}
        )))

(defn -main [& args]
  (let [port 80]
    (run-server app {:port port})
    (println (str "Server started. listen at localhost@" port))))
