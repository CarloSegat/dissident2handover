<template>
    <div style="font-size: var(--my-font-small);">
        <div class="horizontal">
            <slot name="icon"></slot>
            <h3 class="title">{{ entityNameMapping[entityName] || entityName }}</h3>
        </div>
        <div style="margin-top:0.25rem;  font-weight: 100 !important;">
            <slot></slot>
        </div>
    </div>
</template>

<script>
import {entityNameMapping} from "@/constants"
export default {
    props: {
        entityName: {
            type: String,
            required: true
        },
    },
    data() {
        return {
            entityNameMapping
        }
    },
}
</script>

<style scoped>

.title {
    font-size: var(--my-font-big);
    font-weight: 400;
    line-height: 2rem;
}

.horizontal {
    display: flex;
    align-items: flex-end;
}
</style>