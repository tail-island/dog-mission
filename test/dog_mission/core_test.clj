(ns dog-mission.core-test
  (:use     (dog-mission core))
  (:require (clojure     [test :refer :all]))
  (:import  (java.util   Locale)))

(deftest test-translate
  (let [resource-bundle (resource-bundle "dog-mission.message" (Locale. "ja" "JP"))]
    (is (= "今日は、世界！" (translate resource-bundle :hello-world)))
    (is (= "hello-world-2"  (translate resource-bundle :hello-world-2))))
  (let [resource-bundles [(resource-bundle "dog-mission.message"   (Locale. "ja" "JP"))
                          (resource-bundle "dog-mission.message-2" (Locale. "ja" "JP"))]]
    (is (= "今日は、世界！"       (translate resource-bundles :hello-world)))
    (is (= "こんにちは、せかい！" (translate resource-bundles :hello-world-2)))
    (is (= "hello-world-3"        (translate resource-bundles :hello-world-3))))
  (let [resource-bundle (resource-bundle "dog-mission.message" (Locale. "ja" "JP"))]
    (is (= "today is a beautiful day!" (translate resource-bundle :hello "beautiful" "today")))))
