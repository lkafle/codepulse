/*
 * Code Pulse: A real-time code coverage testing tool. For more information
 * see http://code-pulse.com
 *
 * Copyright (C) 2014 Applied Visions - http://securedecisions.avi.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.secdec.codepulse

import language.implicitConversions

import com.secdec.codepulse.data.trace.TraceDataProvider
import com.secdec.codepulse.tracer.TransientTraceDataProvider

import akka.actor.ActorSystem

package object tracer {

	class BootVar[T] {
		private var _value: Option[T] = None
		def apply() = _value getOrElse {
			throw new IllegalStateException("Code Pulse has not booted yet")
		}
		private[tracer] def set(value: T) = {
			_value = Some(value)
		}
	}

	implicit def bootVarToInstance[T](v: BootVar[T]): T = v.apply()

	val traceActorSystem = new BootVar[ActorSystem]
	val traceManager = new BootVar[TraceManager]
	val traceDataProvider = new BootVar[TraceDataProvider]
	val transientTraceDataProvider = new BootVar[TransientTraceDataProvider]
	val traceFileUploadServer = new BootVar[TraceFileUploadHandler]
	val traceAPIServer = new BootVar[TraceAPIServer]

	def boot() {
		traceDataProvider set TraceDataProvider.default
		transientTraceDataProvider set new TransientTraceDataProvider

		val as = TraceManager.defaultActorSystem
		val tm = new TraceManager(as)

		traceActorSystem set as
		traceManager set tm
		traceFileUploadServer set new TraceFileUploadHandler(tm).initializeServer
		traceAPIServer set new TraceAPIServer(tm).initializeServer
	}
}