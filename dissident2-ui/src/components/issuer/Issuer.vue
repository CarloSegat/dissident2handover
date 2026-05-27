<script setup>
    import { issuerDid, issuerUrl } from '@/constants';
    import IssuerTable from '@/components/issuer/IssuerTable.vue';
    import EntityDetails from '@/components/EntityDetails.vue'
import MyIcon from '@/components/MyIcon.vue';
</script>

<template>
    
    <div class="flex-hor my-shadow" style="font-size: var(--my-font-small);">
        <EntityDetails entityName='DT IDM'>
        
            <template #icon>
                <MyIcon icon="mdi-certificate-outline" name="" iconColor="var(--c-4)" :boxed="false" size="var(--my-font-big2)">
                </MyIcon>
            </template>

            <!-- <hr class="line blue"> -->

            <div class="iconAndTetx">
                <MyIcon icon="mdi-fingerprint" name="" iconColor="var(--c-4)" :boxed="false" size="var(--my-font-medium)">
                </MyIcon>
                <div>{{issuerDid}}</div>
            </div>

            <!-- <div class="iconAndTetx">
                <MyIcon icon="mdi-earth" name="" iconColor="var(--c-4)" :boxed="false" size="var(--my-font-medium)">
                </MyIcon>
                <div>{{issuerUrl}}</div>
            </div> -->
                
            
        </EntityDetails>
        <!-- <MyIcon icon="mdi-refresh" name="" iconColor="var(--c-dlg)" v-bind:boxed="true" :darkShadow="true"></MyIcon> -->
    </div>
        
    <IssuerTable issuerName='DT IDM'></IssuerTable>

    <img src="../../assets/achitecture_diagram.png" class="diagramHai">
</template>


<style scoped>

.blue {
    background-image: linear-gradient(90deg, transparent, rgb(40, 62, 210), transparent);
  background-color: transparent;
}
    
</style>