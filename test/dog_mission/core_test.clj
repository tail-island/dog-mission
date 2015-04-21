(ns dog-mission.core-test
  (:use     (dog-mission core))
  (:require (clojure     [test :refer :all]))
  (:import  (java.util   Locale)))

(deftest test-translate
  (conj-resource-bundle-namespace "dog-mission.message")
  (is (= "今日は、世界！"            (translate (Locale. "ja") :hello-world)))
  (is (= :hello-world-2              (translate (Locale. "ja") :hello-world-2)))
  (is (= "today is a beautiful day!" (translate (Locale. "ja") :hello "beautiful" "today")))
  
  (conj-resource-bundle-namespace "dog-mission.message-2")
  (is (= "今日は、世界！"            (translate (Locale. "ja") :hello-world)))
  (is (= "こんにちは、せかい！"      (translate (Locale. "ja") :hello-world-2)))
  (is (= :hello-world-3              (translate (Locale. "ja") :hello-world-3))))
