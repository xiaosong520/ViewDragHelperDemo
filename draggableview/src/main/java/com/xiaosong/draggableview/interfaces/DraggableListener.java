/*
 * Copyright (C) 2014 Pedro Vicente Gómez Sánchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xiaosong.draggableview.interfaces;

/**
 * 拖拽回调接口
 *
 * @author xiaosong
 */
public interface DraggableListener {

  /**
   * Called when the view is minimized.
   */
  void onClosedToBottom();

  /**
   * Called when the view is closed to the left.
   */
  void onClosedToLeft();

  /**
   * Called when the view is closed to the right.
   */
  void onClosedToRight();

  /**
   * Called when the child view location changed
   * @param top
   */
  void onBackgroundChanged(int top);

  boolean isForbidden();
}
