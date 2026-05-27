<script setup>
    import { apDid, apUrl } from '@/constants';
    import AttachmentTable from '@/components/ap/AttachmentTable.vue';
    import EntityDetails from '@/components/EntityDetails.vue'
    import ServiceRepoTable from './ServiceRepoTable.vue';
    import MyIcon from '@/components/MyIcon.vue';
</script>

<template>
    <div class="flex-hor my-shadow" style="font-size: var(--my-font-medium);">
        <EntityDetails entityName='NETWORK SERVICE CONTROLLER'>
        
            <template #icon>
                <MyIcon icon="mdi-wifi-strength-outline" name="" iconColor="var(--c-4)" :boxed="false" size="var(--my-font-big2)">
                </MyIcon>
            </template>

            <!-- <hr class="line blue"> -->

            <div class="iconAndTetx">
                <MyIcon icon="mdi-fingerprint" name="" iconColor="var(--c-4)" :boxed="false" size="var(--my-font-medium)">
                </MyIcon>
                <div>{{apDid}}</div>
            </div>

            <!-- <div class="iconAndTetx">
                <MyIcon icon="mdi-earth" name="" iconColor="var(--c-4)" :boxed="false" size="var(--my-font-medium)">
                </MyIcon>
                <div>{{apUrl}}</div>
            </div>
             -->

            
        </EntityDetails>
        <!-- <MyIcon icon="mdi-refresh" name="" iconColor="var(--c-dlg)" v-bind:boxed="true" :darkShadow="true"></MyIcon> -->
    </div>
    
    <AttachmentTable></AttachmentTable>
    <br>
    <ServiceRepoTable></ServiceRepoTable>
    <img src="../../assets/achitecture_diagram.png" class="diagramHai">
    
</template>


<style scoped>

.blue {
    background-image: linear-gradient(90deg, transparent, rgb(40, 62, 210), transparent);
  background-color: transparent;
}
    
</style>