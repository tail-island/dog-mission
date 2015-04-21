(ns dog-mission.core
  (:require (clojure       [string :as string])
            (clj-time      [coerce :as time.coerce]))
  (:import  (clojure.lang  BigInt Ratio)
            (java.io       InputStreamReader BufferedReader)
            (java.lang     Long Double)
            (java.math     BigDecimal)
            (java.text     DateFormat NumberFormat)
            (java.util     Locale MissingResourceException Properties PropertyResourceBundle ResourceBundle ResourceBundle$Control TimeZone)
            (org.joda.time DateTime LocalDateTime)))

(def ^:dynamic *locale*
  (Locale/getDefault))

(def ^:dynamic *time-zone*
  (TimeZone/getDefault))

(def ^:private resource-bundle-namespaces
  (atom nil))

(defn conj-resource-bundle-namespace
  [& namespace-strings]
  (swap! resource-bundle-namespaces #(concat (map (fn [namespace-string] (string/replace namespace-string \- \_)) namespace-strings)  ; TODO: 「!」とか「?」とか「'」とかについても考える？
                                             %)))

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

(defn- resource-bundles
  []
  (map #(ResourceBundle/getBundle % *locale* utf-8-encoding-control) @resource-bundle-namespaces))

(defprotocol L10nFormat
  (l10n-format [this]))

(extend-protocol L10nFormat
  DateTime
  (l10n-format [this]
    (-> (doto (DateFormat/getDateTimeInstance DateFormat/MEDIUM DateFormat/MEDIUM *locale*)
          (.setTimeZone *time-zone*))
        (.format (time.coerce/to-date this))))
  BigDecimal
  (l10n-format [this]
    (-> (doto (NumberFormat/getInstance *locale*)
          (.setMaximumFractionDigits (.scale this))
          (.setMinimumFractionDigits (.scale this)))
        (.format this)))
  BigInt
  (l10n-format [this]
    (-> (NumberFormat/getInstance *locale*)
        (.format (.toBigInteger this))))
  Long
  (l10n-format [this]
    (-> (NumberFormat/getInstance *locale*)
        (.format this)))
  Double
  (l10n-format [this]
     (-> (NumberFormat/getInstance *locale*)
         (.format this)))
  Ratio
  (l10n-format [this]
    (-> (NumberFormat/getInstance *locale*)
        (.format (.doubleValue this)))))

(defn translate
  ([x]
   (let [key ((if (instance? clojure.lang.Named x)
                name
                str)
              x)]
     (or (->> (resource-bundles)
              (some #(and (.containsKey % key) (.getString % key))))
         (cond-> x
           (satisfies? L10nFormat x) (l10n-format)))))
  ([x & format-arguments]
   (apply format (translate x) format-arguments)))
