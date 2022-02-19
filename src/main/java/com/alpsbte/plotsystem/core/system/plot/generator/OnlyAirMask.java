package com.alpsbte.plotsystem.core.system.plot.generator;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;

public class OnlyAirMask extends ExistingBlockMask{

	private static final long serialVersionUID = 1L;

	public OnlyAirMask(Extent extent) {
		super(extent);
	}

	@Override
	public boolean test(Vector vector) {
		return this.getExtent().getLazyBlock(vector).getType() == 0;
	}
}
