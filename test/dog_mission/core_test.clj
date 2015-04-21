(ns dog-mission.core-test
  (:use     (dog-mission core))
  (:require (clojure     [test :refer :all])
            (clj-time    [core :as    time]))
  (:import  (java.util   Locale TimeZone)))

(deftest test-translate
  (binding [*locale*    (Locale. "ja")
            *time-zone* (TimeZone/getTimeZone "GMT+9")]

    (is (= "2015/01/01 9:00:00"               (translate (time/date-time 2015 1 1 0 0 0))))
    (is (= "123,456"                          (translate 123456)))
    (is (= "123,456.789"                      (translate 123456.78901)))  ; doubleの場合は桁が取れないので、小数点以下はデフォルトの三桁になります。
    (is (= "12,345,678,901,234,567,890"       (translate 12345678901234567890)))
    (is (= "12,345,678,901,234,567,890.12345" (translate 12345678901234567890.12345M)))
    (is (= "3,333.333"                        (translate (/ 10000 3))))  ; BigDecimalだと割り切れなくてエラーになる可能性があるので、doubleに変換しています。
    (is (= "xxx"                              (translate "xxx")))
    (is (= :xxx                               (translate :xxx)))
    (is (= nil                                (translate nil)))
    
    (conj-resource-bundle-namespace "dog-mission.message")
    (is (= "今日は、世界！"            (translate :hello-world)))
    (is (= "today is a beautiful day!" (translate :hello "beautiful" "today")))
    
    (conj-resource-bundle-namespace "dog-mission.message-2")
    (is (= "今日は、世界！"            (translate :hello-world)))
    (is (= "こんにちは、せかい！"      (translate :hello-world-2)))))
