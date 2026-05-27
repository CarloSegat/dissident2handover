<template>
  <v-expansion-panels class="seq my-shadow" v-model="panel">
    <v-expansion-panel class="swag">
      <v-expansion-panel-title color="var(--c-1)" class="collapsableSectionTitle">

        INTERACTIONS</v-expansion-panel-title>
      <v-expansion-panel-text class="swag fullHeight" eager ref="scrollableDiv">
        <div style="display: unset;">
          <div class="stickyContainer">
            <div class="innerSticky" v-if="this.isStickyHeadlineVisible1 && this.isStickyHeadlineVisible">
              <div>CUSTOMER</div>
              <div style="position: relative;right: 1rem;">QR GENERATOR</div>
              <div style="position: relative;right: 3.4rem;">NSC</div>
              <div style="position: relative;right: 3.75rem;">DT IDM</div>
              <div style="position: relative;right: 6rem;">MAGENTA IDM</div>
              <div style="position: relative;right: 1rem;">DLG</div>
            </div>
            <div class="innerSticky" v-if="this.isStickyHeadlineVisible2 && this.isStickyHeadlineVisible">
              <div>CUSTOMER</div>
              <div style="position: relative;right: 4rem;">QR GENERATOR</div>
              <div style="position: relative;right: 1rem;">NSC</div>
              <div style="position: relative;right: 2.4rem;">DT IDM</div>
              <div style="position: relative;right: 5.3rem;">MAGENTA IDM</div>
              <div style="position: relative;right: 1rem;">DLG</div>
            </div>
            <div class="innerSticky" v-if="this.isStickyHeadlineVisible3 && this.isStickyHeadlineVisible">
              <div style="position: relative;right: 1rem;">CUSTOMER</div>
              <div style="position: relative;right: 1rem;">QR GENERATOR</div>
              <div style="position: relative;left: 1rem;">NSC</div>
              <div style="position: relative;right: 0.5rem;">DT IDM</div>
              <div style="position: relative;right: 4rem;">MAGENTA IDM</div>
              <div style="position: relative;right: 0.5rem;">DLG</div>
            </div>
          </div>
        </div>
        <div ref="mermaidRefId" class="mermaid fullHeight">
        </div>
      </v-expansion-panel-text>
    </v-expansion-panel>
  </v-expansion-panels>
</template>

<script>
import mermaid from 'mermaid';
import { useCounterStore } from '@/stores/counterStore';
import { storeToRefs } from 'pinia';
import MyIcon from '@/components/MyIcon.vue';
import { ref } from 'vue';

export default {
  setup() {
    const counterStore = useCounterStore();
    const { mermaidString } = storeToRefs(counterStore)
    const { fetchMermaidSequenceDiagram } = counterStore
    return { mermaidString, fetchMermaidSequenceDiagram }
  },
  mounted() {
    this.fetchMermeidPeroidically();
    mermaid.initialize({ startOnLoad: true });
    window.addEventListener("scroll", this.onScroll);
  },
  unmounted() {
    clearInterval(this.$options.intervalId);
  },
  beforeDestroy() {
    if (this.$options.intervalId) {
      clearInterval(this.$options.intervalId); // Clear the interval timer when component is destroyed
    }
    window.removeEventListener("scroll", this.onScroll)
  },
  data() {
    return {
      panel: [0],
      oldSvg: "",
      isStickyHeadlineVisible: false,
      isStickyHeadlineVisible1: false,
      isStickyHeadlineVisible2: false,
      isStickyHeadlineVisible3: false,
      scrollLevel: 0,
    }
  },
  intervalId: null,
  methods: {
    updateHeadline(){
      let a = document.querySelector("#id_will_be_injected")
      const h = parseInt(a.height.baseVal.value)

      console.log(">>> ", h);

      this.isStickyHeadlineVisible1 = false
      this.isStickyHeadlineVisible2 = false
      this.isStickyHeadlineVisible3 = false

      if (h > 630) {
        this.isStickyHeadlineVisible1 = true && this.isStickyHeadlineVisible
        this.isStickyHeadlineVisible2 = false
        this.isStickyHeadlineVisible3 = false
      }
      if (h > 1525) {
        console.log("this.isStickyHeadlineVisible2 = true && this.isStickyHeadlineVisible", true && this.isStickyHeadlineVisible);
        this.isStickyHeadlineVisible1 = false
        this.isStickyHeadlineVisible2 = true && this.isStickyHeadlineVisible
        this.isStickyHeadlineVisible3 = false
      }
      if (h > 2210) {
        console.log(
          "3333"
        );
        this.isStickyHeadlineVisible1 = false
        this.isStickyHeadlineVisible2 = false
        this.isStickyHeadlineVisible3 = true && this.isStickyHeadlineVisible
      }
    },
    onScroll(e) {
      window.top.scrollY /* or: e.target.documentElement.scrollTop */
      if (parseInt(window.top.scrollY) > 630) {
        this.isStickyHeadlineVisible = true;
        this.updateHeadline();
      } else {
        this.isStickyHeadlineVisible = false;
      }
    },
    async fetchMermeidPeroidically() {
      clearInterval(this.$options.intervalId);
      this.fetchMermaidSequenceDiagram(); // Fetch immediately on component creation
      this.$options.intervalId = setInterval(this.fetchMermeidPeroidically, 2000);

      if (this.oldSvg == this.mermaidString) {
        return;
      }

      // mermeid.render will insert the SVG in the ref provided as 3rd argument, if you 
      // dont provide anything it appends to the <body> and this causes the whole
      // page to re-render !
      let r;
      try {
        r = await mermaid.render('id_will_be_injected', this.mermaidString, this.$refs.mermaidRefId);
      } catch (error) {
        console.log("there wasa a syntax error rendering the mermeid sequence diagram");
        r = await mermaid.render('id_will_be_injected', this.mermaidString, this.$refs.mermaidRefId);
        this.$refs.mermaidRefId.innerHTML = "";
        return;
      }

      console.log(this.oldSvg.slice(200, 210), "<--->", this.mermaidString.slice(200, 210));

      if (this.$refs.mermaidRefId) {
        this.$refs.mermaidRefId.innerHTML = r["svg"];
        console.log("setting new SVG ", r);
        this.oldSvg = this.mermaidString;

        this.$nextTick(() => {

          // this.findStatusRectByText('NEITHER "CUSTOMER" NOR "QR GENERATOR" ARE CONNECTED');

          this.updateHeadline();

          // var textElement = document.querySelector('text.messageText');

          // if (textElement) {
          //   // Define the SVG namespace
          //   var svgNS = "http://www.w3.org/2000/svg";
          //   // Create an SVG <a> element
          //   var aElement = document.createElementNS(svgNS, 'a');
          //   // Set the xlink:href attribute to define the link target
          //   aElement.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href", "https://www.example.com");
          //   aElement.setAttribute("target", "_blank"); // Open in a new tab, if desired

          //   // Insert the <a> element before the <text> element in the DOM
          //   textElement.parentNode.insertBefore(aElement, textElement);
          //   // Move the <text> element inside the <a> element
          //   aElement.appendChild(textElement);
          // }

          // Make rounded rectangles
          const rects = document.querySelectorAll('rect');
          // Iterate through each rect and set the attributes
          rects.forEach(rect => {
            rect.setAttribute('rx', '20px');
            rect.setAttribute('ry', '20px');
            rect.setAttribute('stroke-linejoin', 'round');
          });

        });
      } else {
        console.log("the mermeid REF is null  sefwersgregertahrteww54grw54er");
      }

    },
    findStatusRectByText(textToSearch) {
      const texts = document.querySelectorAll('text');
      texts.forEach(text => {
          if (text.textContent === textToSearch) {
              const parentGroup = text.parentElement;
              const rect = parentGroup.querySelector('rect');
              console.log(rect);
              // rect.style.fill = 'red';
          }
      });
    }
  },
  // watch: {
  //   mermaidStringComputed: {
  //     handler() {
  //       console.log("mermaidString is watched >>>>> >>>>> >>>>> >>>>>");
  //       this.renderMermaid();
  //     },
  //     immediate: true,
  //   }
  // },
};
</script>

<style>
.seq {
  max-width: var(--max-width);
  height: fit-content;
}

.actor {
  stroke: #ffffff !important;
  fill: white !important;
}

tspan {
  fill: var(--c-4) !important;
  font-size: var(--my-font-big);
}

.stickyContainer {
  position: sticky;
  top: 0;
  padding: 1rem;
  padding-bottom: 0.5rem;
  background-color: #ffffff
}

.headlines {
  width: 96%;
  position: absolute;
  left: 0.5rem;
}

.innerSticky {
  display: flex;
  gap: 2rem;
  justify-content: space-around;
  color: var(--c-4);
  font-weight: 400;
}

.note {
  fill:aqua;
}
</style>