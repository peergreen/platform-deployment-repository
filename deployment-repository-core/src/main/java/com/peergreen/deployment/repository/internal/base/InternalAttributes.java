/**
 * Copyright 2013 Peergreen S.A.S. All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peergreen.deployment.repository.internal.base;

import com.peergreen.deployment.repository.Attributes;
import com.peergreen.deployment.repository.view.Facade;
import com.peergreen.deployment.repository.view.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mohammed Boukada
 */
public class InternalAttributes implements Attributes {

    private final Map<String, Object> attributes;
    private final Map<Class<?>, Object> views;


    public InternalAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.views = new HashMap<Class<?>, Object>();
        addDefaultViews();
    }

    protected void addDefaultViews() {
        addView(new RepositoryView(this), Repository.class);
        addView(new FacadeView(this), Facade.class);
    }

    public void addView(Object o,  Class<?>... classes) {
        for (Class<?> clazz : classes) {
            views.put(clazz, o);
        }
    }

    @Override
    public <T> T getAttribute(String attributeName) {
        return (T) this.attributes.get(attributeName);
    }

    @Override
    public <T> T as(Class<T> clazz) {
        return clazz.cast(views.get(clazz));
    }
}
