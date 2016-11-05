/*
 * Copyright 2013-2015 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.about;

import org.gdg.frisbee.android.api.model.Contributor;
import org.gdg.frisbee.android.api.model.Translator;


public class TranslatorsFragment extends ContributorsFragment {

    private static final Contributor[] TRANSLATORS = new Contributor[]{
        new Translator("Friedger Müffke",
            "friedger",
            "https://www.gravatar.com/avatar/72c15c247727ba65f72de3c7c58c4a42?s=170",
            103),
        new Translator("Said Tahsin Dane",
            "tasomaniac",
            "https://www.gravatar.com/avatar/67be1b058c66ed002ff45a1f9a22c0ff?s=170",
            147),
        new Translator("Sebastian Mauer",
            "mauimauer",
            "https://www.gravatar.com/avatar/b9236795d95774ca2137bb15d54da0a9?s=170",
            13),
        new Translator("",
            "BernardoSK",
            "",
            9),
        new Translator("Juan Perez",
            "clinicadentalbolivia",
            "",
            1),
        new Translator("cesarnog",
            "cesarnogueira1210)",
            "https://www.gravatar.com/avatar/58423be6be405d14a4ddfe1bb86cf986?s=170",
            154),
        new Translator("Stefan Hoth",
            "stefanhoth",
            "https://www.gravatar.com/avatar/7a0ee8000984f7966ec7ad227c2f1b56?s=170",
            0),
        new Translator("François Bacconnet",
            "tchoa91",
            "",
            94),
        new Translator("Jhoon Saravia",
            "jhoonsar",
            "https://www.gravatar.com/avatar/5635ba213391ff1947a6641615fe4354?s=170",
            86),
        new Translator("Erick Mendonça",
            "erickmendonca",
            "https://www.gravatar.com/avatar/87402530330e8d1fb2ff707a6f4510ad?s=170",
            8),
        new Translator("Csaba Toth",
            "MrCsabaToth",
            "http://www.gravatar.com/avatar/cc87a10a1c3fb5129a4cf5c2a701b1b2?s=170",
            186),
        new Translator("Riccardo Ciovati",
            "rciovati",
            "http://www.gravatar.com/avatar/80b5a7a78e7e143264b05fc33a6f381e?s=170",
            27),
        new Translator("Somrani Mariem",
            "somranimariem8",
            "http://www.gravatar.com/avatar/8ab786fcd1aaa96c5268ba4870790384?s=170",
            154),
        new Translator("Frank R.",
            "renfeng",
            "http://www.gravatar.com/avatar/4bffd542821e8a3e5e36a0eec7bea0af?s=170",
            59),
        new Translator("Oleh Zasadnyy",
            "bilenkuyboy",
            "http://www.gravatar.com/avatar/2ab351a2bffc95411b0d0d1ff0ef4a6c?s=170",
            67),
        new Translator("Martin Liersch",
            "goddchen",
            "http://www.gravatar.com/avatar/fdc7eb99d52639e67ca31ffe478abd7b?s=170",
            33),
        new Translator("Milos Marinkovic",
            "milosmns",
            "http://www.gravatar.com/avatar/2a1adcb8ed924ecd1045fd6252a7df18?s=170",
            140)
    };

    protected void loadContributors() {
        mAdapter.addAll(TRANSLATORS);
    }
}
