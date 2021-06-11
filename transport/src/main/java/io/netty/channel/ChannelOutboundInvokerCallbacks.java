/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel;

import io.netty.util.internal.StringUtil;

import static java.util.Objects.requireNonNull;

final class ChannelOutboundInvokerCallbacks {

    private ChannelOutboundInvokerCallbacks() { }

    static boolean isNotValidCallback(Channel channel, ChannelOutboundInvokerCallback callback) {
        if (callback instanceof ChannelPromise) {
            return isNotValidPromise(channel, (ChannelPromise) callback);
        }
        return false;
    }

    static boolean isNotValidPromise(Channel channel, ChannelPromise promise) {
        requireNonNull(promise, "promise");

        if (promise.isDone()) {
            // Check if the promise was cancelled and if so signal that the processing of the operation
            // should not be performed.
            //
            // See https://github.com/netty/netty/issues/2349
            if (promise.isCancelled()) {
                return true;
            }
            throw new IllegalArgumentException("promise already done: " + promise);
        }

        if (promise.channel() != channel) {
            throw new IllegalArgumentException(String.format(
                    "promise.channel does not match: %s (expected: %s)", promise.channel(), channel));
        }

        if (promise.getClass() == DefaultChannelPromise.class) {
            return false;
        }

        if (promise instanceof AbstractChannel.CloseFuture) {
            throw new IllegalArgumentException(
                    StringUtil.simpleClassName(AbstractChannel.CloseFuture.class) + " not allowed in a pipeline");
        }
        return false;
    }
}
