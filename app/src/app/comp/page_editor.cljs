
(ns app.comp.page-editor
  (:require-macros [respo.macros :refer [defcomp cursor-> <> span div a style pre]])
  (:require [hsl.core :refer [hsl]]
            [respo.render.html :refer [style->string]]
            [respo-ui.style :as ui]
            [respo-ui.style.colors :as colors]
            [respo.core :refer [create-comp]]
            [respo.comp.space :refer [=<]]
            [respo.comp.inspect :refer [comp-inspect]]
            [app.comp.bookmark :refer [comp-bookmark]]
            [app.comp.expr :refer [comp-expr style-expr]]
            [app.comp.leaf :refer [style-leaf]]
            [app.style :as style]))

(def style-status (merge ui/row {:justify-content :space-between, :padding "0 8px"}))

(def style-watchers (merge ui/row {:display :inline-block}))

(def style-nothing
  {:color (hsl 0 0 100 0.4), :padding "0 16px", :font-family "Josefin Sans"})

(def style-active {:color (hsl 0 0 100)})

(def style-missing
  {:font-family "Josefin Sans", :color (hsl 10 60 50), :font-size 20, :font-weight 100})

(def ui-missing (div {:style style-missing} (<> span "Expression is missing!" nil)))

(def style-stack {:width 200})

(def style-hint {:color (hsl 0 0 100 0.6), :font-family "Josefin Sans"})

(def style-area {:overflow :auto, :padding-bottom 80, :padding-top 40, :flex 1})

(def style-container {:position :relative})

(def style-editor (merge ui/flex ui/column))

(def style-watcher {:color (hsl 0 0 100 0.7), :margin "0 8px"})

(defn on-toggle [state] (fn [e d! m!] (m! (not state))))

(def style-beginner
  {:color (hsl 0 0 100 0.5), :font-family "Josefin Sans", :font-weight 100, :cursor :pointer})

(defcomp
 comp-page-editor
 (states stack router-data pointer)
 (let [state (or (:data states) false)]
   (div
    {:style (merge ui/row ui/flex style-container)}
    (div
     {:style style-stack}
     (if (empty? stack)
       (<> span "Nothing selected" style-nothing)
       (->> stack
            (map-indexed
             (fn [idx bookmark] [idx (comp-bookmark bookmark idx (= idx pointer))])))))
    (=< 8 nil)
    (div
     {:style style-editor}
     (let [others (->> (:others router-data) (vals) (map :focus) (into #{}))
           expr (:expr router-data)]
       (div
        {:style style-area}
        (style {:innerHTML (str ".cirru-expr {" (style->string style-expr) "}")})
        (style {:innerHTML (str ".cirru-leaf {" (style->string style-leaf) "}")})
        (if (some? expr)
          (cursor->
           :root
           comp-expr
           states
           expr
           (:focus router-data)
           []
           others
           false
           false
           state)
          (if (not (empty? stack)) ui-missing))))
     (div
      {:style style-status}
      (div
       {}
       (<> span (str "Others(" (count (:others router-data)) ")") style-hint)
       (div
        {:style style-watchers}
        (->> (:others router-data)
             (vals)
             (map (fn [info] [(:session-id info) (<> span (:name info) style-watcher)])))))
      (div
       {}
       (div
        {:style (merge style-beginner (if state style-active)),
         :on {:click (on-toggle state)}}
        (<> span "Beginner" nil))))
     (comment comp-inspect "Expr" router-data style/inspector)))))
