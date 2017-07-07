
(ns app.comp.header
  (:require-macros [respo.macros :refer [defcomp <> span div]])
  (:require [hsl.core :refer [hsl]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]))

(defn on-search [e d! m!] (d! :router/change {:name :search}))

(defn on-profile [e dispatch!]
  (dispatch! :router/change {:name :profile, :data nil, :router nil}))

(def style-header
  {:height 48,
   :justify-content :space-between,
   :padding "0 16px",
   :font-size 20,
   :color :white,
   :border-bottom (str "1px solid " (hsl 0 0 30)),
   :font-family "Josefin Sans",
   :font-weight 100})

(defn on-files [e dispatch! m!] (dispatch! :router/change {:name :files}))

(defn on-editor [e d! m!] (d! :router/change {:name :editor}))

(def style-pointer {:cursor "pointer"})

(defn on-members [e d! m!] (d! :router/change {:name :members}))

(def style-entry {:cursor :pointer, :width 80})

(defcomp
 comp-header
 (logged-in?)
 (div
  {:style (merge ui/row-center style-header)}
  (div
   {:style ui/row}
   (div {:on {:click on-files}, :style style-entry} (<> span "Files" nil))
   (div {:on {:click on-editor}, :style style-entry} (<> span "Editor" nil))
   (div {:on {:click on-search}, :style style-entry} (<> span "Search" nil))
   (div {:on {:click on-members}, :style style-entry} (<> span "Members" nil)))
  (div
   {:style style-pointer, :on {:click on-profile}}
   (<> span (if logged-in? "Me" "Guest") nil))))
