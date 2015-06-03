(ns dog-mission.core
  (:require (clojure       [string :as string])
            (clj-time      [coerce :as time.coerce]))
  (:import  (clojure.lang  BigInt Compiler Ratio)
            (java.io       InputStreamReader BufferedReader)
            (java.text     DateFormat NumberFormat)
            (java.util     Locale PropertyResourceBundle ResourceBundle ResourceBundle$Control TimeZone UUID)
            (org.joda.time DateTime DateTimeZone)))

(def ^:dynamic *locale*
  (Locale/getDefault))

(def ^:dynamic *time-zone*
  (TimeZone/getDefault))

(def ^:dynamic *joda-time-zone*
  (DateTimeZone/getDefault))

(def ^:private resource-bundle-namespaces
  (atom nil))

(defn conj-resource-bundle-namespace
  [& namespace-strings]
  (swap! resource-bundle-namespaces #(concat (map (fn [namespace-string] (Compiler/munge namespace-string)) namespace-strings)
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

(defn number-format
  []
  (NumberFormat/getInstance *locale*))

(defn date-format
  []
  (doto (DateFormat/getDateInstance DateFormat/MEDIUM *locale*)
    (.setTimeZone *time-zone*)))

(defn date-time-format
  []
  (doto (DateFormat/getDateTimeInstance DateFormat/MEDIUM DateFormat/MEDIUM *locale*)
    (.setTimeZone *time-zone*)))

(defprotocol L10nFormat
  (l10n-format [this]))

(extend-protocol L10nFormat
  DateTime
  (l10n-format [this]
    (.format (date-time-format) (time.coerce/to-date this)))
  BigDecimal
  (l10n-format [this]
    (.format (doto (number-format)
               (.setMaximumFractionDigits (.scale this))
               (.setMinimumFractionDigits (.scale this)))
             this))
  BigInt
  (l10n-format [this]
    (.format (number-format) (.toBigInteger this)))
  Long
  (l10n-format [this]
    (.format (number-format) this))
  Double
  (l10n-format [this]
     (.format (number-format) this))
  Ratio
  (l10n-format [this]
    (.format (number-format) (.doubleValue this)))
  UUID
  (l10n-format [this]
    (str this))
  String
  (l10n-format [this]
    this))

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
