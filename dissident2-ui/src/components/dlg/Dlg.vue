<script setup>
import EntityDetails from '../EntityDetails.vue';
import { dlgDid, dlgUrl } from '@/constants';
import DidResolutionTable from './DidResolutionTable.vue';
import MyIcon from '@/components/MyIcon.vue';
</script>

<template>
    <div class="flex-hor my-shadow" style="font-size: var(--my-font-small);">
        <EntityDetails entityName='DISTRIBUTED LEDGER GATEWAY (DLG)'>
        
            <template #icon>
                <MyIcon icon="mdi-cube-outline" name="" iconColor="var(--c-4)" :boxed="false" size="var(--my-font-big2)">
                </MyIcon>
            </template>

            <div class="iconAndTetx">
                <MyIcon icon="mdi-fingerprint" name="" iconColor="var(--c-4)" :boxed="false" size="var(--my-font-medium)">
                </MyIcon>
                <div>{{dlgDid}}</div>
            </div>

            <!-- <div class="iconAndTetx">
                <MyIcon icon="mdi-earth" name="" iconColor="var(--c-4)" :boxed="false" size="var(--my-font-medium)">
                </MyIcon>
                <div>{{dlgUrl}}</div>
            </div> -->
                
            
        </EntityDetails>
        <!-- <MyIcon icon="mdi-refresh" name="" iconColor="var(--c-dlg)" v-bind:boxed="true" :darkShadow="true"></MyIcon> -->
    </div>

    <DidResolutionTable></DidResolutionTable>
    <img src="../../assets/achitecture_diagram.png" class="diagramHai">

</template>

<script>

export default {
    components: { EntityDetails }
}
</script>
<style scoped>
</style>