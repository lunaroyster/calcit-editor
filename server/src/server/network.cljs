
(ns server.network
  (:require [cljs.nodejs :as nodejs]
            [cljs.reader :as reader]
            [cljs.core.async :refer [chan >!]]
            [server.twig.container :refer [twig-container]]
            [recollect.diff :refer [diff-twig]]
            [recollect.twig :refer [render-twig]]
            ["chalk" :as chalk]
            [server.util.detect :refer [port-taken?]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defonce socket-registry (atom {}))

(defonce server-chan (chan))

(def shortid (js/require "shortid"))

(def ws (js/require "uws"))

(def WebSocketServer (.-Server ws))

(defn handle-message [op op-data session-id]
  (let [op-id (.generate shortid), op-time (.valueOf (js/Date.))]
    (go (>! server-chan [op op-data session-id op-id op-time]))))

(defn run-server! [configs]
  (let [port (:port configs)]
    (port-taken?
     port
     (fn [err taken?]
       (if (some? err)
         (do (.error js/console err) (.exit js/process 1))
         (if taken?
           (do
            (println
             (.red
              chalk
              (str
               "Failed to start server, port "
               port
               " is in use!\nYou can try `port="
               (inc port)
               " cumulo-editor`.")))
            (.exit js/process 1))
           (let [wss (new WebSocketServer (js-obj "port" port))]
             (.on
              wss
              "connection"
              (fn [socket]
                (let [session-id (.generate shortid)]
                  (handle-message :session/connect nil session-id)
                  (swap! socket-registry assoc session-id socket)
                  (println (.gray chalk (str "client connected: " session-id)))
                  (.on
                   socket
                   "message"
                   (fn [rawData]
                     (let [action (reader/read-string rawData), [op op-data] action]
                       (handle-message op op-data session-id))))
                  (.on
                   socket
                   "close"
                   (fn []
                     (println (.gray chalk (str "client disconnected: " session-id)))
                     (swap! socket-registry dissoc session-id)
                     (handle-message :session/disconnect nil session-id))))))
             (println
              "Server started, please edit on"
              (.blue chalk (str "http://cumulo-editor.cirru.org?port=" port)))))))))
  server-chan)

(def diff-options {:key :id})

(defonce client-caches (atom {}))

(defn render-clients! [db]
  (doseq [session-entry (:sessions db)]
    (let [[session-id session] session-entry
          old-store (or (get @client-caches session-id) nil)
          new-store (render-twig (twig-container db session) old-store)
          changes (diff-twig old-store new-store diff-options)
          socket (get @socket-registry session-id)]
      (comment .info js/console "Changes for" session-id ":" (clj->js changes))
      (if (and (not (empty? changes)) (some? socket))
        (do (.send socket (pr-str changes)) (swap! client-caches assoc session-id new-store))))))
