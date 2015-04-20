# dog-mission

Easy i18n library for Clojure. Just wrap the Java ResourceBundle.

## Usage

Add following to your <code>project.clj</code>.

```clojure
[com.tail-island/twin-spar "0.1.0"]
```

Create properties file with UTF-8 encoding.

```properties
hello-world=hello, world!
has-format-message=%2$s is a %1$s day!
```

Save properties file as <code>src/<i>your-namespace-1</i>/<i>your-name-space-2</i>.properties</code>.

Translate message like below code.

```clojure
(ns your-name-space-1.core
  (:require (dog-mission [core :as dog-mission])))

(def resource-bundle
  (dog-mission/resource-bundle "your-name-space-1.your-name-space-2" (Locale "en" "US")))

(println (dog-mission/translate resource-bundle :hello-world))
(println (dog-mission/translate resource-bundle :has-format-message "beautiful" "today"))
```

## License

Copyright © 2015 OJIMA Ryoji

Distributed under the Eclipse Public License either version 1.0 or any later version.
