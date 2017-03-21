/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ml.puredark.hviewer.libraries.advrecyclerview.common.data;

import ml.puredark.hviewer.ui.dataproviders.AbstractDataProvider;

public abstract class AbstractExpandableDataProvider<G extends AbstractExpandableDataProvider.GroupData, C extends AbstractExpandableDataProvider.ChildData> {
    public abstract int getGroupCount();

    public abstract int getChildCount(int groupPosition);

    public abstract G getGroupItem(int groupPosition);

    public abstract C getChildItem(int groupPosition, int childPosition);

    public abstract void moveGroupItem(int fromGroupPosition, int toGroupPosition);

    public abstract void moveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition);

    public abstract void removeGroupItem(int groupPosition);

    public abstract void removeChildItem(int groupPosition, int childPosition);

    public static abstract class BaseData extends AbstractDataProvider.Data {
        public abstract String getText();
    }

    public static abstract class GroupData extends BaseData {
        public abstract boolean isSectionHeader();

        public abstract long getGroupId();
    }

    public static abstract class ChildData extends BaseData {
        public abstract long getChildId();
    }
}
