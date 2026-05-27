<template>
    <div class="outer">

        <div class="logoContainerSmall">
            <img alt="Dissident 2 logo" class="logoDissident" src="@/assets/DISSIDENT_Logo.jpg" width="65rem">
        </div>
     
        <div class="titlediss">
            <div class="demonstrator">DEMONSTRATOR</div>
            TRUST & SECURITY FOR A SERVICE FEDERATION SCENARIO
        </div>

        <div class="snetAndTub">
            <div class="boxTub">
                <img alt="TUB logo" style="margin-right:1rem; object-fit: contain;" src="@/assets/tublogo.png" width="50rem" />
            </div> 
            <div class="boxTub mr-2">
                <img alt="TUB logo" src="@/assets/snet-logo.png" width="55rem" style="object-fit: contain; margin-right:1rem;"/>
            </div>
        </div>
       
    </div>

</template>
<script>
export default {

}
</script>
<style scoped>

.vline {
  background-image: linear-gradient(transparent, var(--c-4), transparent);
  background-color: transparent;
  border-width: 0;
  height: 45px;
  width:1px;
}
    
.boxTub {
  border-radius: var(--my-border-radius);
  /* display: flex; */
  /* flex-direction: row-reverse; */
    /* gap: var(--padding-l); */
}

.outer {
    display: flex;
    background-color: white;
    /* padding: var(--padding-s); */
    align-items: center;
    filter: drop-shadow(0 0.75px 6px black);
    justify-content: space-between;
    display: grid;
    grid-template-columns: 5.5rem auto;
    justify-content: start;
}

.titlediss {
    color: black;
    padding-left: var(--padding-l);;
    font-size: var(--my-font-big2);
    font-weight: 400;
}

.snetAndTub {
    height: 100%;
    display: flex;
    position: absolute;
    right: 15rem;
    flex-direction: column;
    justify-content: space-around;
}

.logoDissident {
    widows: 10rem
    
}


.logoContainerSmall {
    display: flex;
    flex-direction: column;
    align-items: center;
    /* width: 15rem; */
}

.demonstrator{
    font-size: var(--my-font-big);
    margin-bottom: -0.8rem;
}

    

</style>