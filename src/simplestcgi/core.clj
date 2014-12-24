(ns simplestcgi.core
 (:use [org.httpkit.server :only [run-server]]
       [compojure.core :only [defroutes GET POST]]
       [ring.middleware.params :only [wrap-params]]
       [clojure.java.shell :only [sh]])
 (:require [clojure.string :as string]))

(defn view-form []
  (str "<html><head></head><body>"
   "<form method=\"post\" action='/'>"
   "subdomain <input type=\"text\" name=\"subdomain\"/><br/>"
   "github-repo <input type=\"text\" name=\"githubrepo\"/>"
   "<input type=\"submit\"/>"
   "</form></body></html>"))

(defn kick-docker [subdomain githubrepo]
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
                     ;(sh "sh" "-c" (str "/usr/bin/docker -H tcp://" strHostIp ":4243 ps"))
                     (sh "sh" "-c"
                         (str "/usr/bin/docker -H tcp://" strHostIp ":4243 run "
                              "-d -v /var/run/docker.sock:/var/run/docker.sock "
                              "-e VIRTUAL_HOST=nginx-proxy.cloudapp.net "
                              "-e MAX_CONTAINERS=5 "
                              "-e POOL_BASE_DOMAIN=" subdomain ".gennai.org "
                              "-e GITHUB_BOT=false "
                              "-e VIRTUAL_HOST=*." subdomain ".gennai.org "
                              "-e PREVIEW_REPOSITORY_URL=" githubrepo " "
                              "--name pool-" subdomain " "
                              "mookjp/pool"))
                     "<br/>"
                     subdomain "<br/>"
                     githubrepo "<br/>"
                     "</DIV>")}
        )))

(defroutes routes
  (POST "/" [subdomain githubrepo] (kick-docker subdomain githubrepo))
  (GET  "/" [] (view-form)))

(def app (wrap-params routes))

(defn -main [& args]
  (let [port 80]
    (run-server app {:port port})
    (println (str "Server started. listen at localhost@" port))))
