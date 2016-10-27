function imports() {

}

function help() {

	return "Free an allocated barrier\n" +
			"Parameters: bid\n" +
			"  bid: Id of an allocated barrier to free";
}

function exec(bid) {

    if (bid == null) {
        dxterm.printlnErr("No bid specified");
        return;
    }

    var sync = dxram.service("sync");

    if (!sync.barrierFree(bid)) {
        dxterm.printlnErr("Freeing barrier failed");
    } else {
        dxterm.printfln("Barrier 0x%X free'd", bid);
    }
}
