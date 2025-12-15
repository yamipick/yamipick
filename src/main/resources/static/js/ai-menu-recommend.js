const tagList = [
    { label: "#매콤", value: "spicy" },
    { label: "#달달", value: "sweet" },
    { label: "#짭짤", value: "salty" },
    { label: "#고기", value: "meat" },
    { label: "#해산물", value: "seafood" },
    { label: "#국물요리", value: "soup" },
    { label: "#면요리", value: "noodle" },
    { label: "#기름진", value: "oily" },
    { label: "#건강식", value: "healthy" }
];

let selectedTags = [];

// 태그 버튼 그리기
const tagArea = document.getElementById("tagArea");

const row1 = document.createElement("div");
row1.classList.add("tag-row");

const row2 = document.createElement("div");
row2.classList.add("tag-row");

tagList.forEach((tag, index) => {
    const btn = document.createElement("button");
    btn.classList.add("tag-btn");
    btn.innerText = tag.label;

	btn.addEventListener("click", () => {
	        btn.classList.toggle("selected");

	        if (btn.classList.contains("selected")) {
	            selectedTags.push(tag.value);
	        } else {
	            selectedTags = selectedTags.filter(t => t !== tag.value);
	        }

	        fetchRecommend();
	    });

	    if (index < 5) row1.appendChild(btn);
	    else row2.appendChild(btn);
	});

tagArea.appendChild(row1);
tagArea.appendChild(row2);

// 추천 API 호출
async function fetchRecommend() {

    const res = await fetch("/api/ai/recommend/menu", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ positiveTags: selectedTags })
    });

    const data = await res.json();
    renderResults(data);
}

function renderResults(list) {
    if (!list || list.length === 0) {
        document.getElementById("resultArea").innerHTML = "<p>추천 결과가 없습니다.</p>";
        return;
    }

    let html = "<h2 class='result-title'>추천 메뉴</h2><div class='menu-list'>";

    list.forEach(item => {
        html += `
            <div class="menu-card">
				<div class="menu-image">
                	<img src="${item.menuImage}" alt="">
					<div class="menu-hover-desc">
						${item.menuDescription}
					</div>
				</div>
				
                <div class="menu-info">
                    <h3>${item.menuName}</h3>
                    <div class="tag-badges">
						${item.allTags.map(t => `<span class="tag-badge">${convertTag(t)}</span>`).join("")}
                    </div>
                </div>
            </div>
        `;
    });

    html += "</div>";
    document.getElementById("resultArea").innerHTML = html;
}

function convertTag(tag) {
    const mapping = {
        spicy: "#매콤",
        sweet: "#달달",
        salty: "#짭짤",
        meat: "#고기",
        seafood: "#해산물",
        soup: "#국물요리",
        noodle: "#면요리",
        oily: "#기름진",
        healthy: "#건강식"
    };
    return mapping[tag] ?? tag;
}

