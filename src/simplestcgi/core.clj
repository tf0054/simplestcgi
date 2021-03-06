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
      (let [strKlCmd (str "/usr/bin/docker -H tcp://" strHostIp ":4243 kill "
                        "pool-" subdomain)
            strRmCmd (str "/usr/bin/docker -H tcp://" strHostIp ":4243 rm "
                        "pool-" subdomain)
            strUpCmd (str "/usr/bin/docker -H tcp://" strHostIp ":4243 run "
                        "-d -v /var/run/docker.sock:/var/run/docker.sock "
                        "-e MAX_CONTAINERS=3 "
                        "-e POOL_BASE_DOMAIN=" subdomain ".gennai.org "
                        "-e GITHUB_BOT=false "
                        "-e VIRTUAL_HOST=*." subdomain ".gennai.org "
                        "-e PREVIEW_REPOSITORY_URL=" githubrepo " "
                        "--name pool-" subdomain " "
                        "mookjp/pool")]
          {:status  200
           :headers {"Content-Type" "text/html"}
           :body    (str "<DIV STYLE=\"font-family: Consolas, Menlo, 'Liberation Mono', Courier, monospace;\">"
                         ;(sh "sh" "-c" (str "/usr/bin/docker -H tcp://" strHostIp ":4243 ps"))
                         "Command:"
                         "<pre style=\"border-style: solid ; border-width: 1px;\">"
                         strKlCmd ";<br/>"
                         strRmCmd ";<br/>"
                         strUpCmd
                         "</pre>"
                         "Response:" "<br/>"
                         "<pre style=\"border-style: solid ; border-width: 1px;\">"
                         (sh "sh" "-c" strKlCmd) "<br/>"
                         (sh "sh" "-c" strRmCmd) "<br/>"
                         (sh "sh" "-c" strUpCmd)
                         "</pre>"
                         "Link:" "<br/>"
                         "<a href=\"http://master." subdomain ".gennai.org/\" target=\"_blank\">"
                         "http://master." subdomain ".gennai.org/" "</a>" "<br/>"
                         "</DIV>")})
        )))

(defroutes routes
  (POST "/" [subdomain githubrepo] (kick-docker subdomain githubrepo))
  (GET  "/" [] (view-form)))
  (GET  "/ps" [] (view-form)))

(def app (wrap-params routes))

(defn -main [& args]
  (let [port 80]
    (run-server app {:port port})
    (println (str "Server started. listen at localhost@" port))))
