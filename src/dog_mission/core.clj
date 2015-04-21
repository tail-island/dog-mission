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

(defn- resource-bundle
  [namespace-string locale]
  (ResourceBundle/getBundle namespace-string locale utf-8-encoding-control))

(def ^:private resource-bundle-namespaces
  (atom nil))

(defn conj-resource-bundle-namespace
  [& namespace-strings]
  (swap! resource-bundle-namespaces #(concat (map (fn [namespace-string] (string/replace namespace-string \- \_)) namespace-strings)  ; TODO: 「!」とか「?」とか「'」とかについても考える？
                                             %)))

(defn translate
  ([locale message-key]
   (let [key ((if (instance? clojure.lang.Named message-key)
                name
                str)
              message-key)]
     (or (->> @resource-bundle-namespaces
              (map #(resource-bundle % locale))
              (some #(and (.containsKey % key) (.getString % key))))
         message-key)))
  ([locale message-key & format-arguments]
   (apply format (translate locale message-key) format-arguments)))
