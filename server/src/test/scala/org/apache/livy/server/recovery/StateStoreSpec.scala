/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.livy.server.recovery

import org.scalatest.BeforeAndAfter
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

import org.apache.livy.{LivyBaseUnitTestSuite, LivyConf}
import org.apache.livy.sessions.SessionManager

class StateStoreSpec extends AnyFunSpec with Matchers 
    with BeforeAndAfter with LivyBaseUnitTestSuite {
  describe("StateStore") {
    after {
      StateStore.cleanup()
    }

    def createConf(stateStore: String): LivyConf = {
      val conf = new LivyConf()
      conf.set(LivyConf.RECOVERY_MODE.key, SessionManager.SESSION_RECOVERY_MODE_RECOVERY)
      conf.set(LivyConf.RECOVERY_STATE_STORE.key, stateStore)
      conf
    }

    it("should throw an error on get if it's not initialized") {
      intercept[AssertionError] { StateStore.get }
    }

    it("should initialize blackhole state store if recovery is disabled") {
      StateStore.init(new LivyConf())
      StateStore.get shouldBe a[BlackholeStateStore]
    }

    it("should pick the correct store according to state store config") {
      StateStore.pickStateStore(createConf("filesystem")) shouldBe classOf[FileSystemStateStore]
      StateStore.pickStateStore(createConf("zookeeper")) shouldBe classOf[ZooKeeperStateStore]
    }

    it("should return error if an unknown recovery mode is set") {
      val conf = new LivyConf()
      conf.set(LivyConf.RECOVERY_MODE.key, "unknown")
      intercept[IllegalArgumentException] { StateStore.init(conf) }
    }

    it("should return error if an unknown state store is set") {
      intercept[IllegalArgumentException] { StateStore.init(createConf("unknown")) }
    }
  }
}
