;   Copyright (c) Rich Hickey. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
;DM: Modifed for ClojureCLR by David Miller
(ns clojure.tools.nrepl.response-test
  (:use clojure.test
        [clojure.tools.nrepl.transport :only (piped-transports) :as t])
  (:require [clojure.tools.nrepl :as repl])
  )                                                                    ;DM: (:import (java.util.concurrent BlockingQueue LinkedBlockingQueue TimeUnit)))

(deftest response-seq
  (let [[local remote] (piped-transports)]
    (doseq [x (range 10)] (t/send remote x))
    (is (= (range 10) (repl/response-seq local 0)))
    
    ; ensure timeouts don't capture later responses
    (repl/response-seq local 100)
    (doseq [x (range 10)] (t/send remote x))
    (is (= (range 10) (repl/response-seq local 0)))))

(deftest client
  (let [[local remote] (piped-transports)
        client (repl/client local 100)]
    (doseq [x (range 10)] (t/send remote x))
    (is (= (range 10) (client 17)))
    (is (= 17 (t/recv remote)))))

(deftest client-heads
  (let [[local remote] (piped-transports)
        client (repl/client local Int32/MaxValue)                       ;DM: Long/MAX_VALUE
        all-seq (client)]
    (doseq [x (range 10)] (t/send remote x))
    (is (= [0 1 2] (take 3 all-seq)))
    (is (= (range 3 7) (take 4 (client :a))))
    (is (= :a (t/recv remote)))
    (is (= (range 10) (take 10 all-seq)))))