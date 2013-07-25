/*
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

import java.util.Collection;

/**
 * @author Mohammed Boukada
 */
public class FacadeView implements Facade {

    private Attributes attributes;

    public FacadeView(Attributes attributes) {
        this.attributes = attributes;
    }

    @Override
    public Collection<String> getUrls() {
        return attributes.getAttribute(AttributesName.URLS);
    }
}
