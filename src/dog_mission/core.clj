(ns dog-mission.core
  (:require (clojure   [string :as string]))
  (:import  (java.io   InputStreamReader BufferedReader)
            (java.util Locale MissingResourceException Properties PropertyResourceBundle ResourceBundle ResourceBundle$Control)))

(def ^:private utf-8-encoding-control
  (proxy [ResourceBundle$Control] []
    (newBundle [base-name locale format class-loader reload?]
      (let [resource-name (.toResourceName this (.toBundleName this base-name locale) "properties")]
        (with-open [input-stream (if reload?
                                   (-> (doto (.openConnection (.getResource class-loader resource-name))
                                         (.setUseCaches false))
                                       (.getInputStream))
                                   (.getResourceAsStream class-loader resource-name))]
          (PropertyResourceBundle. (BufferedReader. (InputStreamReader. input-stream "UTF-8"))))))))

(defn resource-bundle
  [namespace-string locale]
  (ResourceBundle/getBundle (string/replace namespace-string \- \_) locale utf-8-encoding-control))

(defn translate
  ([resource-bundles message-key]
   (let [message (name message-key)]
     (letfn [(translate' [resource-bundle]
               (if (.containsKey resource-bundle message)
                 (.getString resource-bundle message)))]
       (or (some translate'
                 (cond-> resource-bundles
                   (not (coll? resource-bundles)) (vector resource-bundles)))
           message))))
  ([resource-bundles message-key & format-arguments]
   (apply format (translate resource-bundles message-key) format-arguments)))
