const $=id=>document.getElementById(id),money=n=>new Intl.NumberFormat('kk-KZ',{maximumFractionDigits:0}).format(n)+' ₸';
// Singleton: бүкіл сайтқа ортақ бір қойма.
class LogisticsCenter{static instance;static getInstance(){return this.instance??=new LogisticsCenter()}constructor(){if(LogisticsCenter.instance)return LogisticsCenter.instance;this.cargos=[];this.messages=[];this.counter=0;try{let d=JSON.parse(localStorage.getItem('aerolink-v1'));if(d&&Array.isArray(d.cargos)){this.cargos=d.cargos.filter(c=>typeof c.id==='string'&&typeof c.client==='string'&&typeof c.route==='string'&&Number.isFinite(c.weight)&&c.weight>0&&['standard','express'].includes(c.service)&&Number.isInteger(c.state)&&c.state>=0&&c.state<=3);this.messages=Array.isArray(d.messages)?d.messages.filter(m=>typeof m.text==='string'&&typeof m.time==='string').slice(0,50):[];this.counter=Math.max(Number(d.counter)||0,...this.cargos.map(c=>Number(c.id.slice(4))||0))}}catch{}LogisticsCenter.instance=this}save(){try{localStorage.setItem('aerolink-v1',JSON.stringify({cargos:this.cargos,messages:this.messages,counter:this.counter}));return true}catch{return false}}}
// Observer: тіркелу және мәртебе өзгерісін тыңдау.
class EventBus{constructor(){this.listeners=[]}subscribe(fn){this.listeners.push(fn)}notify(c){this.listeners.forEach(fn=>fn(c))}}
// Strategy: тариф алгоритмін таңдау.
class StandardDelivery{calculate(w){return w*1500}}
class ExpressDelivery{calculate(w){return w*2500}}
const strategies={standard:new StandardDelivery(),express:new ExpressDelivery()};
// State: әр күй өзінің келесі күйін анықтайды.
class RegisteredState{label='Тіркелді';next(){return 1}}
class ReadyState{label='Ұшуға дайын';next(){return 2}}
class TransitState{label='Жолда';next(){return 3}}
class DeliveredState{label='Жеткізілді';next(){return 3}}
const states=[new RegisteredState(),new ReadyState(),new TransitState(),new DeliveredState()];
// Facade: интерфейс үшін тіркеу мен күй ауыстыруды біріктіру.
class AirLogisticsFacade{constructor(center,bus){this.center=center;this.bus=bus}register(client,route,weight,service){if(!client.trim()||!Number.isFinite(weight)||weight<.1||weight>10000||!strategies[service])throw Error('Деректерді тексеріңіз.');let cargo={id:'AIR-'+String(++this.center.counter).padStart(3,'0'),client:client.trim(),route,weight,service,state:0};this.center.cargos.unshift(cargo);this.bus.notify(cargo);this.center.save();return cargo}advance(id){let c=this.center.cargos.find(c=>c.id===id);if(!c||c.state===3)return false;c.state=states[c.state].next();this.bus.notify(c);this.center.save();return true}}
const center=LogisticsCenter.getInstance(),bus=new EventBus(),facade=new AirLogisticsFacade(center,bus);bus.subscribe(c=>{center.messages.unshift({text:c.client+': '+c.id+' — '+states[c.state].label,time:new Date().toLocaleTimeString('kk-KZ',{hour:'2-digit',minute:'2-digit'})});center.messages=center.messages.slice(0,50)});
function toast(text){$('toast').textContent=text;$('toast').style.display='block';clearTimeout(toast.timer);toast.timer=setTimeout(()=>$('toast').style.display='none',3500)}
function estimate(){let w=Number($('weight').value);$('price').textContent=Number.isFinite(w)&&w>0?money(strategies[$('service').value].calculate(w)):'—'}
function td(text){let el=document.createElement('td');el.textContent=text;return el}
function render(){ $('total').textContent=center.cargos.length;$('transit').textContent=center.cargos.filter(c=>c.state===2).length;$('delivered').textContent=center.cargos.filter(c=>c.state===3).length;let q=$('search').value.toLocaleLowerCase(),filter=$('filter').value;let list=center.cargos.filter(c=>(c.id+' '+c.client).toLocaleLowerCase().includes(q)&&(filter==='all'||String(c.state)===filter));$('rows').replaceChildren();for(let c of list){let tr=document.createElement('tr'),cell=td(''),title=document.createElement('strong'),sub=document.createElement('small');title.textContent=c.id;sub.textContent=c.client;cell.append(title,sub);tr.append(cell,td(c.route));let value=td(money(strategies[c.service].calculate(c.weight))),detail=document.createElement('small');detail.style.display='block';detail.textContent=c.weight+' кг · '+(c.service==='standard'?'Стандартты':'Жедел');value.append(detail);tr.append(value);let status=td(''),badge=document.createElement('span');badge.className='status'+(c.state===3?' done':'');badge.textContent=states[c.state].label;status.append(badge);tr.append(status);let action=td(''),btn=document.createElement('button');btn.className='next';btn.disabled=c.state===3;btn.textContent=c.state===3?'Аяқталды':'Келесі кезең →';btn.onclick=()=>{facade.advance(c.id);render();toast(c.id+' — '+states[c.state].label)};action.append(btn);tr.append(action);$('rows').append(tr)}$('empty').style.display=list.length?'none':'block';$('empty').textContent=center.cargos.length?'Сүзгіге сәйкес жүк табылмады.':'Әзірге жүк жоқ. Алғашқы жүкті тіркеңіз.';$('notices').replaceChildren();for(let m of center.messages){let li=document.createElement('li'),time=document.createElement('time');time.textContent=m.time;li.append(time,document.createTextNode(m.text));$('notices').append(li)}$('noNotice').style.display=center.messages.length?'none':'block'}
$('form').onsubmit=e=>{e.preventDefault();try{let c=facade.register($('client').value,$('route').value,Number($('weight').value),$('service').value);$('search').value='';$('filter').value='all';render();toast(c.id+' тіркелді'+(center.save()?'':'. Браузерде сақтау қолжетімсіз.'))}catch(err){toast(err.message)}};$('weight').oninput=estimate;$('service').onchange=estimate;$('search').oninput=render;$('filter').onchange=render;
const patterns=[{name:'Singleton',category:'ҚҰРУ ПАТТЕРНІ',description:'Кластың тек бір данасын жасайды. Бағдарламаның барлық бөлігі бір ортақ объектімен жұмыс істейді.',application:'Авиа логистикада LogisticsCenter барлық жүкті бір орталықта сақтайды. getInstance() әр шақырылғанда сол объектіні қайтарады.',example:'Сайтта: тіркелген жүктер мен көрсеткіштер бір LogisticsCenter қоймасынан алынады.',code:`class LogisticsCenter {
    private static final LogisticsCenter INSTANCE =
        new LogisticsCenter();

    private LogisticsCenter() {}

    public static LogisticsCenter getInstance() {
        return INSTANCE;
    }
}

// Екі айнымалы да бір объектіні көрсетеді.
LogisticsCenter a = LogisticsCenter.getInstance();
LogisticsCenter b = LogisticsCenter.getInstance();
System.out.println(a == b); // true`},{name:'Observer',category:'МІНЕЗ-ҚҰЛЫҚ ПАТТЕРНІ',description:'Объект өзгергенде оған жазылған бақылаушылар хабар алады. Бір өзгеріске бірнеше бақылаушы жауап бере алады.',application:'Жүктің мәртебесі өзгерген сайын клиентке оның нөмірі мен жаңа мәртебесі жіберіледі.',example:'Сайтта: «Келесі кезең» батырмасын басыңыз. Клиент хабарламаларына жаңа жазба қосылады.',code:`interface CargoObserver {
    void update(String id, String status);
}

class Client implements CargoObserver {
    public void update(String id, String status) {
        System.out.println(id + " — " + status);
    }
}

// Cargo класындағы әдістер:
void addObserver(CargoObserver observer) {
    observers.add(observer);
}
void notifyObservers() {
    for (CargoObserver observer : observers) {
        observer.update(id, state.getName());
    }
}`},{name:'State',category:'МІНЕЗ-ҚҰЛЫҚ ПАТТЕРНІ',description:'Объектінің әрекеті ағымдағы күйіне байланысты өзгереді. Әр күйдің ережесі жеке класқа жазылады.',application:'Жүк тіркеледі, ұшуға дайындалады, жолға шығады және жеткізіледі. Соңғы күйден кейін алға жылжу тоқтайды.',example:'Сайтта: төрт кезеңнен өтіңіз. «Жеткізілді» күйінде әрекет батырмасы өшеді.',code:`interface CargoState {
    String getName();
    void next(Cargo cargo);
}

class RegisteredState implements CargoState {
    public String getName() { return "Тіркелді"; }
    public void next(Cargo cargo) {
        cargo.setState(new ReadyState());
    }
}

class DeliveredState implements CargoState {
    public String getName() { return "Жеткізілді"; }
    public void next(Cargo cargo) {
        // Соңғы күй: әрі қарай ауыспайды.
    }
}`},{name:'Strategy',category:'МІНЕЗ-ҚҰЛЫҚ ПАТТЕРНІ',description:'Бір тапсырманы орындаудың бірнеше алгоритмін жеке кластарға бөліп, қажет тәсілді таңдауға мүмкіндік береді.',application:'Жеткізу бағасы стандартты тарифпен немесе жедел тарифпен есептеледі. Жүк объектісін өзгерту қажет емес.',example:'Сайтта: 10 кг үшін стандартты баға — 15 000 ₸, жедел баға — 25 000 ₸. Тәсілді ауыстырып салыстырыңыз.',code:`interface DeliveryStrategy {
    double calculatePrice(double weight);
}

class StandardDelivery implements DeliveryStrategy {
    public double calculatePrice(double weight) {
        return weight * 1500;
    }
}
class ExpressDelivery implements DeliveryStrategy {
    public double calculatePrice(double weight) {
        return weight * 2500;
    }
}
DeliveryStrategy strategy = new ExpressDelivery();
System.out.println(strategy.calculatePrice(10));`},{name:'Facade',category:'ҚҰРЫЛЫМДЫҚ ПАТТЕРН',description:'Бірнеше ішкі жүйенің жұмысын қарапайым интерфейстің артына біріктіреді. Қолданушы күрделі қадамдарды өзі басқармайды.',application:'Бір registerCargo() әдісі жүк жасайды, клиентті бақылаушы ретінде қосады, орталыққа тіркейді және баға есептейді.',example:'Сайтта: «Жүкті тіркеу» батырмасы бір әрекетпен тіркеуді, хабарламаны және тізілімді жаңартуды орындайды.',code:`class AirLogisticsFacade {
    public Cargo registerCargo(
        String id, double weight, String route,
        CargoObserver client,
        DeliveryStrategy strategy
    ) {
        Cargo cargo = new Cargo(id, weight, route);
        cargo.addObserver(client);
        LogisticsCenter.getInstance().addCargo(cargo);
        System.out.println(
            strategy.calculatePrice(weight)
        );
        cargo.notifyObservers();
        return cargo;
    }
}`}];
function selectPattern(index){let p=patterns[index];$('category').textContent=p.category;$('patternTitle').textContent=p.name;$('description').textContent=p.description;$('application').textContent=p.application;$('liveExample').textContent=p.example;$('code').textContent=p.code;$('patternPanel').setAttribute('aria-labelledby','tab-'+index);[...$('tabs').children].forEach((b,i)=>{b.classList.toggle('active',i===index);b.setAttribute('aria-selected',i===index);b.tabIndex=i===index?0:-1})}patterns.forEach((p,i)=>{let b=document.createElement('button');b.textContent=String(i+1).padStart(2,'0')+' / '+p.name;b.id='tab-'+i;b.setAttribute('role','tab');b.setAttribute('aria-controls','patternPanel');b.onclick=()=>selectPattern(i);b.onkeydown=e=>{if(['ArrowLeft','ArrowRight','Home','End'].includes(e.key)){e.preventDefault();let n=e.key==='Home'?0:e.key==='End'?4:(i+(e.key==='ArrowRight'?1:4))%5;selectPattern(n);$('tabs').children[n].focus()}};$('tabs').append(b)});$('copy').onclick=async()=>{try{await navigator.clipboard.writeText($('code').textContent);toast('Код көшірілді')}catch{let r=document.createRange();r.selectNodeContents($('code'));let sel=window.getSelection();sel.removeAllRanges();sel.addRange(r);toast('Код белгіленді. Ctrl+C арқылы көшіріңіз.')}};estimate();render();selectPattern(0);
