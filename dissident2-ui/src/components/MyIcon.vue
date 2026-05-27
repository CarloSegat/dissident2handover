<template>
    <div class="flex-row" @click="handleClick" v-bind:class="{ 'flash': isFlashing }">
        <div v-bind:class="{ 'icon': boxed, 'darkShawow': darkShadow}" :style="{ color: iconColor }" >
            <v-icon v-if="icon !== 'spinner'" :icon="this.iconc" :size="this.size" class="marg">
            </v-icon>
            <v-progress-circular v-else
                indeterminate
            ></v-progress-circular>
        </div>
        <div class="okokk">
            <span v-if="name" class="iconText" v-bind:class="{ 'selected': selected}" :style="{ color: iconColor }">
                {{ name }}
            </span>
            <span v-if="this.role" class="smallUnder" v-bind:class="{ 'selectedSmall': selected}" :style="{ color: iconColor }">{{ role }} 
                </span>
        </div>
    </div>
</template>


<script>
export default {
    data(){
        return {
            isFlashing: false
        }
    },
    props: {
        name: {
            type: String,
            required: true
        },
        role: {
            type: String,
            required: true
        },
        icon: {
            type: String,
            required: true
        },
        iconColor: {
            type: String,
            default: '#111' // Default color if none is provided
        },
        boxed: {
            type: Boolean,
            default: true,
        },
        callback: {
            type: Function
        },
        size: {
            type: String,
            default: "large",
        },
        darkShadow: {
            type: Boolean,
            default: false,
        },
        selected: {
            type: Boolean,
            default: false,
        }
    },
    computed: {
        iconc(){
            if(this.selected){
                if(this.icon.includes("wifi")){
                    return "mdi-wifi-strength-4"
                }
                if(this.icon.includes("square")){
                    return "mdi-qrcode"
                }
                return this.icon.slice(0, -8)
            }
            return this.icon;
        }
    },
    methods: {
        handleClick() {
            console.log(">>>> click ");
            this.isFlashing = true;
            setTimeout(() => {
                this.isFlashing = false;
            }, 300); // Duration of the flash effect
            this.callback();
        },
    }
}
</script>

<style scoped>
.icon {
    -webkit-font-smoothing: antialiased;
    display: flex !important;
    box-shadow: -0.2rem .2rem .375rem -.0625rem rgba(255, 255, 255, 0.407), 0 .125rem .25rem -.0625rem rgba(20, 20, 20, .07);
    justify-content: center !important;
    align-items: center !important;
    /* margin-right: .5rem !important; */
    /* background-color: var(--my-white); */
    width: 2.5rem;
    height: 2.5rem;
    border-radius: .5rem;
}

.darkShawow {
    box-shadow: -0.15rem .15rem .3rem -.05rem rgba(0, 0, 0, 0.407), 0 .125rem .25rem -.0625rem rgba(20, 20, 20, .07) !important;
}

.iconText {
    font-size: var(--my-font-medium) !important;
    font-weight: 400;
}

.flex-row {
    display: flex;
    align-items: flex-end;
    z-index: 1;
}

.okokk {
    display: flex;
    flex-direction: column;
    gap: 0rem;
    margin-left: 0.5rem;
}

.selected {
    font-weight:900;
}

.selectedSmall {
    font-weight: 500 !important;
}

.smallUnder{
    font-size: var(--my-font-small) ;
    font-weight: 100;
    line-height: 0.15rem;
}

@keyframes flash {
    /* 0% { background-color: white; }
    50% { background-color: lightgray; }
    100% { background-color: white; } */
    0% { opacity: 100%;; }
    50% { opacity: 50%;; }
    100% { opacity: 100%;; }
}

.flash {
    animation: flash 0.1s;
}

</style>